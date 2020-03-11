package io.fluidsonic.raptor


interface KtorRouteFeature {

	fun RaptorFeatureComponent.setup(target: RaptorConfigurable<KtorRouteRaptorComponent>)
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
