package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.lifecycle.*


private val startableComponentKey = RaptorComponentKey<StartableComponent>("startable")


object StartablePlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(startableComponentKey, StartableComponent())

		lifecycle.onStart("startable") {
			context[Startable.propertyKey]!!.start()
		}

		lifecycle.onStop("startable") {
			context[Startable.propertyKey]!!.stop()
		}
	}
}


@RaptorDsl
val RaptorAssemblyInstallationScope.startable
	get() = componentRegistry.oneOrNull(startableComponentKey) ?: throw RaptorPluginNotInstalledException(StartablePlugin)
