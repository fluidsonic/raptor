package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*


internal class ArgumentResolver(
	private val factoryName: String
) {

	private val currentContext = ThreadLocal<Context>()


	@Suppress("UNCHECKED_CAST")
	private fun Context.resolve(name: String): Any? {
		val scope = execution.raptorContext?.asScope()
			?: return null

		val gqlDefinition = argumentDefinitions.first { it.name == name }
		val definition = checkNotNull(gqlDefinition.raptorArgumentDefinition)

		val valueType = definition.valueType
		val expectsMaybe = valueType.classifier == Maybe::class
		if (expectsMaybe && !argumentValues.containsKey(name))
			return Maybe.nothing

		var value = argumentValues[name]
		if (value != null) {
			val aliasDefinition = gqlDefinition.raptorTypeDefinition as? GraphAliasDefinition<Any, Any>
			if (aliasDefinition != null)
				value = scope.parseAliasValue(value, parse = aliasDefinition.parse, typeRef = gqlDefinition.type)
		}

		if (expectsMaybe)
			value = Maybe.of(value)

		return value
	}


	fun resolveArgument(name: String, variableName: String): Any? {
		val context = currentContext.get()
			?: error("Variable '$variableName' is delegated to argument(\"'$name'\") and can only be accessed within '$factoryName { … }'.")

		return context.resolve(name = name)
	}


	// FIXME Consolidate list handling
	private fun RaptorGraphScope.parseAliasValue(value: Any, parse: RaptorGraphScope.(value: Any) -> Any, typeRef: GTypeRef): Any =
		when (typeRef) {
			is GListTypeRef -> (value as Collection<Any?>).map { element ->
				element?.let { parseAliasValue(it, parse = parse, typeRef = typeRef.elementType) }
			}
			is GNamedTypeRef -> parse(value)
			is GNonNullTypeRef -> parseAliasValue(value, parse = parse, typeRef = typeRef.nullableRef)
		}


	internal inline fun <Result> withArguments(
		argumentValues: Map<String, Any?>,
		argumentDefinitions: Collection<GArgumentDefinition>,
		context: GExecutorContext,
		action: () -> Result
	): Result {
		val previousContext = currentContext.get()

		currentContext.set(Context(
			argumentDefinitions = argumentDefinitions,
			argumentValues = argumentValues,
			execution = context
		))

		try {
			return action()
		}
		finally {
			currentContext.set(previousContext)
		}
	}


	@Suppress("ProtectedInFinal")
	protected class Context(
		val argumentDefinitions: Collection<GArgumentDefinition>,
		val argumentValues: Map<String, Any?>,
		val execution: GExecutorContext
	)
}
