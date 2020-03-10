package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*


interface RaptorFeature {

	fun RaptorFeatureCompletion.complete() = Unit
	fun RaptorFeatureSetup.setup() = Unit
}
