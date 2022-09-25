package tests

import io.fluidsonic.raptor.*


private val startableComponentKey = RaptorComponentKey<StartableComponent>("startable")


object StartablePlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(startableComponentKey, StartableComponent())

		lifecycle.onStart {
			context[Startable.propertyKey]!!.start()
		}

		lifecycle.onStop {
			context[Startable.propertyKey]!!.stop()
		}
	}
}


@RaptorDsl
val RaptorAssemblyInstallationScope.startable
	get() = componentRegistry.oneOrNull(startableComponentKey) ?: throw RaptorPluginNotInstalledException(StartablePlugin)
