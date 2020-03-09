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
	}
}
