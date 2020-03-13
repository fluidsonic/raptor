package io.fluidsonic.raptor


interface KtorRouteFeature {

	fun KtorRouteFeatureSetup.setup()
	fun KtorRouteFeatureSetupCompletion.completeSetup() = Unit
}
