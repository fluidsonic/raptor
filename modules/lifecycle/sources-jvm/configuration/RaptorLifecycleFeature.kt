package io.fluidsonic.raptor

import kotlinx.coroutines.*


public object RaptorLifecycleFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorLifecycleFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(RaptorLifecycleComponent.Key, RaptorLifecycleComponent())

		ifInstalled(raptorDIFeatureId) {
			di {
				provide { get<RaptorContext>()[DefaultRaptorLifecycle.PropertyKey]!!.coroutineContext }
				provide { CoroutineScope(get()) }
			}
		}
	}
}


public const val raptorLifecycleFeatureId: RaptorFeatureId = "raptor.lifecycle"


public val Raptor.lifecycle: RaptorLifecycle
	get() = properties[DefaultRaptorLifecycle.PropertyKey]
		?: error("You must install ${RaptorLifecycleFeature::class.simpleName} for enabling lifecycle functionality.")
