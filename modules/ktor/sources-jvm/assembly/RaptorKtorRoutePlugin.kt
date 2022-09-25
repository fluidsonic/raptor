package io.fluidsonic.raptor.ktor


// TODO Improve naming to avoid confusion with actual RaptorPlugin for Ktor.
public interface RaptorKtorRoutePlugin {

	public fun RaptorKtorRoutePluginConfigurationEndScope.onConfigurationEnded() {}
	public fun RaptorKtorRoutePluginConfigurationStartScope.onConfigurationStarted() {}
}
