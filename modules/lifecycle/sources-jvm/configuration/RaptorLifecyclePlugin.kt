package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


private val lifecycleComponentKey = RaptorComponentKey<RaptorLifecycleComponent>("lifecycle")


public object RaptorLifecyclePlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(lifecycleComponentKey, RaptorLifecycleComponent())

		optional(RaptorDIPlugin) {
			di {
				provide { get<RaptorLifecycle>().coroutineContext }
				provide { context.lifecycle }
			}
		}
	}


	override fun toString(): String = "lifecycle"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorLifecyclePlugin>.lifecycle: RaptorLifecycleComponent
	get() = componentRegistry.oneOrNull(lifecycleComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)
