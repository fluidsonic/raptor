package io.fluidsonic.raptor


object BsonRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		raptorSetupContext.register(BsonRaptorComponent())

		// FIXME bind CodecRegistry
	}
}


// FIXME is it okay to automatically register the feature?
// FIXME component naming
@Raptor.Dsl3
val RaptorFeatureSetup.bson
	get(): RaptorConfigurable<BsonRaptorComponent> {
		install(BsonRaptorFeature) // FIXME check duplicates

		return raptorSetupContext.getOrCreateComponent { BsonRaptorComponent() }
	}
