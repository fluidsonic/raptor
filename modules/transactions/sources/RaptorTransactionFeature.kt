package io.fluidsonic.raptor


object RaptorTransactionFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		propertyRegistry.register(DefaultRaptorContext.PropertyKey, DefaultRaptorContext())
	}


	override fun RaptorFeatureInstallationScope.install() = Unit


	override fun toString() = "transaction feature"
}
