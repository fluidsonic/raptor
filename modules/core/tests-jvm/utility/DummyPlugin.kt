package tests

import io.fluidsonic.raptor.*


object DummyPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {}


	override fun toString() =
		"dummy"
}
