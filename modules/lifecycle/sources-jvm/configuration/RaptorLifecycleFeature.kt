package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import kotlinx.coroutines.*


public object RaptorLifecycleFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(RaptorLifecycleComponent.Key, RaptorLifecycleComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorContext>()[DefaultRaptorLifecycle.PropertyKey]!!.coroutineContext }
				provide { CoroutineScope(get()) }
			}
		}
	}
}


public val Raptor.lifecycle: RaptorLifecycle
	get() = properties[DefaultRaptorLifecycle.PropertyKey]
		?: error("You must install ${RaptorLifecycleFeature::class.simpleName} for enabling lifecycle functionality.")
