package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


internal class DefaultRaptorGraph(
	exceptionHandlers: Collection<GraphExceptionHandler<*>>,
	override val schema: GSchema,
	override val tags: Set<Any>,
) : RaptorGraph {

	private val executor = GExecutor.default(
		exceptionHandler = ExceptionHandler(handlers = exceptionHandlers),
		nodeInputCoercer = NodeInputCoercer,
		schema = schema,
		variableInputCoercer = VariableInputCoercer,
	)


	override suspend fun execute(
		document: GDocument,
		operationName: String?,
		variableValues: Map<String, Any?>,
		context: RaptorTransactionContext,
	): GResult<Map<String, Any?>> =
		executor.execute(
			document = document,
			operationName = operationName,
			variableValues = variableValues,
			extensions = GExecutorContextExtensionSet {
				raptorContext = context
			}
		)


	override fun parse(source: GDocumentSource.Parsable) =
		GDocument.parse(source)
			.flatMapValue { document ->
				val errors = document.validate(schema)
				when {
					errors.isNotEmpty() -> GResult.failure(errors)
					else -> GResult.success(document)
				}
			}


	override fun serialize(result: GResult<Map<String, Any?>>) =
		executor.serializeResult(result)
}
