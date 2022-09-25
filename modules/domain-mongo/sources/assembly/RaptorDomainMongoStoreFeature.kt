package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.cqrs.*


public object RaptorDomainMongoPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		require(RaptorDomainPlugin)
		// require(RaptorMongoPlugin) // FIXME

		require(RaptorBsonPlugin) {
			install(RaptorAggregateEventIdBson)
		}
	}


	override fun toString(): String = "domain-mongo"
}
