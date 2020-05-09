package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorContext : RaptorScope {

	fun createTransaction(): RaptorTransaction
	fun createTransactionKodein(config: Kodein.Builder.() -> Unit = {}): DKodein // FIXME refactor
}
