package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.cqrs.*


public object RaptorDomainMongoFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		requireFeature(RaptorDomainFeature)
		// requireFeature(RaptorMongoFeature) // FIXME

		requireFeature(RaptorBsonFeature) {
			install(RaptorAggregateEventIdBson)
		}
	}


	override fun toString(): String = "domain-mongo"
}
