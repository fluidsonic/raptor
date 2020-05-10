package io.fluidsonic.raptor


object RaptorLifecycleFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorLifecycleComponent.Key, RaptorLifecycleComponent())
	}
}


val Raptor.lifecycle: RaptorLifecycle
	get() = properties[DefaultRaptorLifecycle.PropertyKey]
		?: error("You must install ${RaptorLifecycleFeature::class.simpleName} for enabling lifecycle functionality.")
