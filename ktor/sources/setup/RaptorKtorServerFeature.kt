package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*


interface RaptorKtorServerFeature {

	fun RaptorFeatureSetup.setup(scope: RaptorKtorServerSetup)
}
