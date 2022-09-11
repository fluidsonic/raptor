package io.fluidsonic.raptor.ktor


// TODO Improve naming to avoid confusion with actual RaptorFeature for Ktor.
public interface RaptorKtorRouteFeature {

	public fun RaptorKtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {}
	public fun RaptorKtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {}
}
