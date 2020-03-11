package io.fluidsonic.raptor


interface RaptorFeature {

	fun RaptorFeatureComponent.setup() = Unit
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
