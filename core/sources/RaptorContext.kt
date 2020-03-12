package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorContext {

	val dkodein: DKodein

	fun createScope(): RaptorScope
	fun createTransaction(): RaptorTransaction
}
