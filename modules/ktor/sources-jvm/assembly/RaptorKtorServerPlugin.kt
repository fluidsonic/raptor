package io.fluidsonic.raptor.ktor


// TODO Improve naming to avoid confusion with actual RaptorPlugin for Ktor.
public interface RaptorKtorServerPlugin {

	public fun RaptorKtorServerPluginConfigurationEndScope.onConfigurationEnded() {}
	public fun RaptorKtorServerPluginConfigurationStartScope.onConfigurationStarted() {}
}
