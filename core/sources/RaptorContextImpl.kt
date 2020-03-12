package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorContextImpl(
	override val dkodein: DKodein
) : RaptorContext {

	override fun createScope() =
		RaptorScopeImpl(context = this)


	override fun createTransaction() =
		RaptorTransactionImpl(parentContext = this)
}
