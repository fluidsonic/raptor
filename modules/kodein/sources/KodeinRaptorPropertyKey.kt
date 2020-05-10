package io.fluidsonic.raptor

import org.kodein.di.*


internal object KodeinRaptorPropertyKey : RaptorPropertyKey<Kodein> {

	override fun toString() = "kodein"
}
