package io.fluidsonic.raptor

import com.mongodb.*


object MongoRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		bson {
			registries(MongoClientSettings.getDefaultCodecRegistry())
		}
	}
}
