package io.fluidsonic.raptor

import kotlinx.coroutines.*


internal class GraphInputContext(
	private val arguments: Map<String, Any?>
) {

	@Suppress("UNCHECKED_CAST")
	fun <Value : Any?> argument(name: String) =
		arguments[name] as Value


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
