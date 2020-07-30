package io.fluidsonic.raptor


object RaptorLifecycleFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorLifecycleFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RaptorLifecycleComponent.Key, RaptorLifecycleComponent())
	}
}


const val raptorLifecycleFeatureId: RaptorFeatureId = "raptor.lifecycle"


val Raptor.lifecycle: RaptorLifecycle
	get() = properties[DefaultRaptorLifecycle.PropertyKey]
		?: error("You must install ${RaptorLifecycleFeature::class.simpleName} for enabling lifecycle functionality.")
