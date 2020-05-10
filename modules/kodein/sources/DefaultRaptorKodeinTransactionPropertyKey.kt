package io.fluidsonic.raptor

import org.kodein.di.*


object DefaultRaptorKodeinTransactionPropertyKey : RaptorTransactionPropertyKey<Kodein> {

	override fun toString() = "kodein"
}
