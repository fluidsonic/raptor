package tests

import io.fluidsonic.raptor.*


object CounterPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(CounterComponent.key, CounterComponent())
	}


	override fun toString() =
		"counter"
}


@RaptorDsl
val RaptorAssemblyInstallationScope.counter: CounterComponent
	get() = componentRegistry.oneOrNull(CounterComponent.key) ?: throw RaptorPluginNotInstalledException(CounterPlugin)
