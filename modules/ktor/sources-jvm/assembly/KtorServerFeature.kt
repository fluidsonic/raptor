package io.fluidsonic.raptor

// FIXME improve naming to avoid confusion with actual RaptorFeature for Ktor
// FIXME rename to sync with RaptorFeature
public interface KtorServerFeature {

	public fun KtorServerFeatureConfigurationEndScope.onConfigurationEnded() {}
	public fun KtorServerFeatureConfigurationStartScope.onConfigurationStarted() {}
}
