package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


internal class DefaultRaptorGraphContext(
	override val parent: RaptorTransactionContext,
	val system: DefaultRaptorGraph,
) : RaptorGraphContext {

	override val properties
		get() = parent.properties


	override fun toString() =
		TODO() // FIXME
}
