package io.fluidsonic.raptor


interface RaptorFeature {

	fun RaptorFeatureSetup.setup() = Unit
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
