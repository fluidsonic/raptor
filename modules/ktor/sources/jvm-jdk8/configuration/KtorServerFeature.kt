package io.fluidsonic.raptor


interface KtorServerFeature {

	fun KtorServerFeatureConfigurationEndScope.onConfigurationEnded() = Unit
	fun KtorServerFeatureConfigurationStartScope.onConfigurationStarted() = Unit
}
