package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graphql.internal.*


internal class GraphSystem(
	val schema: GSchema,
	val typeSystem: GraphTypeSystem,
) {

	private val executor = GExecutor.default(
		exceptionHandler = ExceptionHandler(), // FIXME improve
		schema = schema,
		nodeInputCoercer = NodeInputCoercer,
		variableInputCoercer = VariableInputCoercer
	)


	suspend fun execute(
		documentSource: GDocumentSource.Parsable,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		context: RaptorGraphContext,
	): Map<String, Any?> =
		GDocument.parse(documentSource)
			.flatMapValue { document ->
				val errors = document.validate(schema)
				when {
					errors.isNotEmpty() -> GResult.failure(errors)
					else -> GResult.success(document)
				}
			}
			.flatMapValue { document ->
				executor.execute(
					document = document,
					operationName = operationName,
					variableValues = variableValues,
					extensions = GExecutorContextExtensionSet {
						raptorContext = context
					}
				)
			}
			.let { executor.serializeResult(it) }
}
