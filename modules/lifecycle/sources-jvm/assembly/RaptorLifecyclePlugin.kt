package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


public object RaptorLifecyclePlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.lifecycleComponent, RaptorLifecycleComponent())

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
	get() = componentRegistry.oneOrNull(Keys.lifecycleComponent) ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)
