package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.stdlib.*
import kotlinx.coroutines.*


internal class GraphInputContext(
	private val arguments: Map<String, Any?>,
	private val definitions: Collection<GArgumentDefinition>,
	private val system: GraphSystem,
	private val environment: RaptorGraphScope
) {

	@Suppress("UNCHECKED_CAST")
	fun <Value : Any?> argument(name: String): Value {
		val definition = definitions.first { it.name == name }
		val valueType = definition[system.valueTypeExtensionKey]
		val expectsMaybe = valueType?.classifier == Maybe::class
		if (expectsMaybe && !arguments.containsKey(name))
			return Maybe.nothing as Value

		var value = arguments[name]

		if (value != null) {
			@Suppress("UNCHECKED_CAST")
			val aliasDefinition = definition[system.typeDefinitionExtensionKey] as? GraphAliasDefinition<Any, Any>
			if (aliasDefinition != null)
				value = aliasDefinition.parse(environment, value)
		}

		if (expectsMaybe)
			value = Maybe.of(value)

		return value as Value
	}


	suspend fun <Result> use(block: suspend () -> Result) =
		withContext(threadLocalContext.asContextElement(value = this)) {
			block()
		}


	inline fun <Result> useBlocking(block: () -> Result): Result {
		val parentContext = threadLocalContext.get()
		threadLocalContext.set(this)

		try {
			return block()
		}
		finally {
			threadLocalContext.set(parentContext)
		}
	}


	companion object {

		private val threadLocalContext = ThreadLocal<GraphInputContext>()


		val current
			get() = threadLocalContext.get()
				?: error("GraphInputContext.current can only be used in code wrapped by GraphInputContext.use/useBlocking { … }")
		// FIXME This error is actually raised if you try to access an input object argument by delegate outside of the factory. Improve detection/error message here!
	}
}
