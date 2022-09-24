package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


private val lifecycleComponentKey = RaptorComponentKey<RaptorLifecycleComponent>("lifecycle")


public object RaptorLifecycleFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(lifecycleComponentKey, RaptorLifecycleComponent())

		ifFeature(RaptorDIFeature) {
			di {
				provide { get<RaptorLifecycle>().coroutineContext }
				provide { context.lifecycle }
			}
		}
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.lifecycle: RaptorLifecycleComponent
	get() = componentRegistry.oneOrNull(lifecycleComponentKey) ?: throw RaptorFeatureNotInstalledException(RaptorLifecycleFeature)
