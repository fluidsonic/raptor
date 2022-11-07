package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.mongo.*


public object RaptorDomainMongoPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		val domainConfiguration = require(RaptorDomainPlugin)

		configure(RaptorBsonPlugin) {
			bson.definitions(
				RaptorAggregateEventBson.bson(definitions = domainConfiguration.aggregateDefinitions),
				RaptorAggregateEventBson.idBson(),
			)
		}
	}


	override fun RaptorPluginInstallationScope.install() {
		require(RaptorDomainPlugin)
		require(RaptorMongoPlugin)
	}


	override fun toString(): String = "domain-mongo"
}
