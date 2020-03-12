package io.fluidsonic.raptor


interface KtorServerFeature {

	fun RaptorFeatureSetup.setup(target: RaptorConfigurable<KtorServerRaptorComponent>)
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
