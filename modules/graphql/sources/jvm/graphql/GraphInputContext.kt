package io.fluidsonic.raptor

// FIXME
//
//internal class GraphInputContext(
//	private val arguments: Map<String, Any?>,
//	private val definitions: Collection<GArgumentDefinition>,
//	private val system: GraphSystem,
//	private val environment: RaptorGraphScope
//) {
//
//
//
//
//	inline fun <Result> useBlocking(block: () -> Result): Result {
//		val parentContext = threadLocalContext.get()
//		threadLocalContext.set(this)
//
//		try {
//			return block()
//		}
//		finally {
//			threadLocalContext.set(parentContext)
//		}
//	}
//
//
//	companion object {
//
//		private val threadLocalContext = ThreadLocal<GraphInputContext>()
//
//
//		val current
//			get() = threadLocalContext.get()
//				?: error("GraphInputContext.current can only be used in code wrapped by GraphInputContext.use/useBlocking { … }")
//		// FIXME This error is actually raised if you try to access an input object argument by delegate outside of the factory. Improve detection/error message here!
//	}
//}
