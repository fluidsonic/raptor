package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*
import io.fluidsonic.stdlib.*


internal class ArgumentResolver(
	private val factoryName: String,
) {

	private val currentContext = ThreadLocal<Context>() // TODO won't work with coroutines


	// TODO refactor
	private fun Context.resolve(name: String, transforms: List<RaptorGraphInputScope.(Any?) -> Any?>): Any? {
		val context = execution.raptorContext
			?: return null

		val gqlDefinition = argumentDefinitions.first { it.name == name }
		val argument = checkNotNull(gqlDefinition.raptorArgument)

		val expectsMaybe = argument.kotlinType.classifier == Maybe::class
		if (expectsMaybe && !argumentValues.containsKey(name))
			return Maybe.nothing

		val inputScope = object : RaptorGraphInputScope, RaptorTransactionScope by context { // TODO improve

			override fun invalid(details: String?): Nothing =
				invalidValueError("invalid argument ($details)") // TODO improve
		}

		var value = argumentValues[name]?.let { value ->
			if (expectsMaybe && value == Maybe.nothing)
				return value

			val aliasType = gqlDefinition.raptorType as? AliasGraphType
			if (aliasType != null)
				inputScope.parseAliasValue(value, parse = aliasType.convertReferencedToAlias, typeRef = gqlDefinition.type)
			else
				value
		}

		if (expectsMaybe)
			value = Maybe.of(value)

		with(inputScope) {
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
		context: GExecutorContext,
		action: () -> Result,
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
		val execution: GExecutorContext,
	)
}
