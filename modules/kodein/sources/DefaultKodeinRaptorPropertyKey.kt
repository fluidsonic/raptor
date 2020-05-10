package io.fluidsonic.raptor

import org.kodein.di.*


object DefaultKodeinRaptorPropertyKey : RaptorPropertyKey<Kodein> {

	override fun toString() = "kodein"
}
