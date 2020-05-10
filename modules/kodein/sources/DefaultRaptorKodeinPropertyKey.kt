package io.fluidsonic.raptor

import org.kodein.di.*


object DefaultRaptorKodeinPropertyKey : RaptorPropertyKey<Kodein> {

	override fun toString() = "kodein"
}
