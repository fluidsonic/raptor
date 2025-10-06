package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*


public object RaptorMongoPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
//		require(RaptorBsonPlugin) // FIXME
	}


	override fun toString(): String = "mongo2"
}
