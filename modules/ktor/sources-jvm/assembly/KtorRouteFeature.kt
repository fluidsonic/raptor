package io.fluidsonic.raptor


// FIXME improve naming to avoid confusion with actual RaptorFeature for Ktor
public interface KtorRouteFeature {

	// FIXME rename to sync with RaptorFeature
	public fun KtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {}
	public fun KtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {}
}
