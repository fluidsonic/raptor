package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*


object BsonRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		raptorSetupContext.register(BsonRaptorSetup())

		// FIXME bind CodecRegistry
	}
}


fun RaptorFeatureSetup.bson(config: BsonRaptorSetup.() -> Unit) {
	install(BsonRaptorFeature) // FIXME check duplicates

	raptorSetupContext.configure(config)
}
