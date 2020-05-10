package io.fluidsonic.raptor


object RaptorLifecycleFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		val lifecycle = componentRegistry.one(RaptorLifecycleComponent.Key).finalize()

		propertyRegistry.register(DefaultRaptorLifecycle.PropertyKey, lifecycle)

		onCompleted {
			lifecycle.context = context
		}
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(RaptorLifecycleComponent.Key, RaptorLifecycleComponent())
	}
}


val Raptor.lifecycle: RaptorLifecycle
	get() = properties[DefaultRaptorLifecycle.PropertyKey]
		?: error("You must install ${RaptorLifecycleFeature::class.simpleName} in order to control the lifecycle.")
