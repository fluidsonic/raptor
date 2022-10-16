package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.coroutines.*


public object RaptorLifecyclePlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.lifecycleComponent, RaptorLifecycleComponent())

		optional(RaptorDIPlugin) {
			di {
				provide<CoroutineContext> { get<RaptorLifecycle>().coroutineContext }
				provide<RaptorLifecycle> { context.lifecycle }
			}
		}
	}


	override fun toString(): String = "lifecycle"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorLifecyclePlugin>.lifecycle: RaptorLifecycleComponent
	get() = componentRegistry.oneOrNull(Keys.lifecycleComponent) ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)
