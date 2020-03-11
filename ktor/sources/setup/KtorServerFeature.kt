package io.fluidsonic.raptor


interface KtorServerFeature {

	fun RaptorFeatureComponent.setup(target: RaptorConfigurable<KtorServerRaptorComponent>)
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
