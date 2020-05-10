package io.fluidsonic.raptor


object RaptorTransactionFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		propertyRegistry.register(DefaultRaptorTransactionFactory.PropertyKey, componentRegistry.one(RaptorTransactionComponent.Key).finalize())
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(RaptorTransactionComponent.Key, RaptorTransactionComponent())
	}


	override fun toString() = "transaction feature"
}
