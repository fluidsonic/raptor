package io.fluidsonic.raptor


interface KtorServerFeature {

	fun KtorServerFeatureSetup.setup() = Unit
	fun KtorServerFeatureSetupCompletion.completeSetup() = Unit
}
