package io.fluidsonic.raptor


interface KtorRouteFeature {

	fun RaptorFeatureSetup.setup(target: RaptorConfigurable<KtorRouteRaptorComponent>)
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
