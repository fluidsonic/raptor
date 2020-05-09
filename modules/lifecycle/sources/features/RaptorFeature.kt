package io.fluidsonic.raptor


// FIXME should this also be a RaptorComponent?
interface RaptorFeature {

	fun RaptorFeatureSetup.setup() = Unit
	fun RaptorFeatureSetupCompletion.completeSetup() = Unit
}
