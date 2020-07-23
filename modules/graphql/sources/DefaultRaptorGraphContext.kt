package io.fluidsonic.raptor


internal class DefaultRaptorGraphContext(
	override val parent: RaptorTransactionContext,
	val system: GraphSystem
) : RaptorGraphContext {

	override val properties
		get() = parent.properties


	override fun toString() =
		TODO() // FIXME
}
