package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorTransactionContextImpl(
	override val dkodein: DKodein,
	parentContext: RaptorContextImpl
) : RaptorTransactionContext, RaptorContext by parentContext {

	override fun createScope() =
		RaptorTransactionScopeImpl(context = this)
}
