package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


internal object RaptorGraphsPropertyKey : RaptorPropertyKey<Collection<RaptorGraph>> {

	override fun toString() = "graphs"
}
