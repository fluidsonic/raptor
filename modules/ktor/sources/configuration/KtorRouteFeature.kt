package io.fluidsonic.raptor


interface KtorRouteFeature {

	fun KtorRouteFeatureConfigurationEndScope.onConfigurationEnded() = Unit
	fun KtorRouteFeatureConfigurationStartScope.onConfigurationStarted() = Unit
}
