package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class ArgumentResolver(
	private val factoryName: String,
) {

	private val currentContext = ThreadLocal<Context>() // TODO won't work with coroutines


	// TODO refactor
	private fun Context.resolve(name: String, transforms: List<RaptorGraphInputScope.(Any?) -> Any?>): Any? {
		val gqlDefinition = argumentDefinitions.first { it.name == name }

		var value = argumentValues[name]?.let { value ->
			val aliasType = gqlDefinition.raptorType as? AliasGraphType
			if (aliasType != null)
				GraphInputScope.parseAliasValue(value, parse = aliasType.convertReferencedToAlias, typeRef = gqlDefinition.type)
			else
				value
		}

		with(GraphInputScope) {
			for (transform in transforms)
				value = transform(value)
		}

		return value
	}


	fun resolveArgument(name: String, variableName: String, transforms: List<RaptorGraphInputScope.(Any?) -> Any?> = emptyList()): Any? {
		val context = currentContext.get()
			?: error("Variable '$variableName' is delegated to argument(\"'$name'\") and can only be accessed within '$factoryName { … }'.")

		return context.resolve(name = name, transforms = transforms)
	}


	// TODO Consolidate list handling
	private fun RaptorGraphInputScope.parseAliasValue(value: Any, parse: RaptorGraphInputScope.(input: Any) -> Any, typeRef: GTypeRef): Any =
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
		action: () -> Result,
	): Result {
		val previousContext = currentContext.get()

		currentContext.set(
			Context(
				argumentDefinitions = argumentDefinitions,
				argumentValues = argumentValues,
			)
		)

		try {
			return action()
		}
		finally {
			currentContext.set(previousContext)
		}
	}


	internal class Context(
		val argumentDefinitions: Collection<GArgumentDefinition>,
		val argumentValues: Map<String, Any?>,
	)
}
