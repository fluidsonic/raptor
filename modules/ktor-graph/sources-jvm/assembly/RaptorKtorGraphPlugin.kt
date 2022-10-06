package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*


public object RaptorKtorGraphPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		require(RaptorGraphPlugin)
		require(RaptorKtorPlugin)
	}


	override fun toString(): String = "ktor-graph"
}
