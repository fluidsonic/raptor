package io.fluidsonic.raptor.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*


public object RaptorMongoPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		require(RaptorBsonPlugin)
	}


	override fun toString(): String = "mongo"
}
