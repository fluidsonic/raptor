package io.fluidsonic.raptor.ktor


// TODO Improve naming to avoid confusion with actual RaptorFeature for Ktor.
public interface RaptorKtorServerFeature {

	public fun RaptorKtorServerFeatureConfigurationEndScope.onConfigurationEnded() {}
	public fun RaptorKtorServerFeatureConfigurationStartScope.onConfigurationStarted() {}
}
