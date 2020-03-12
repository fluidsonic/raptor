package io.fluidsonic.raptor

import org.kodein.di.*


internal class KtorServerContextImpl(
	override val dkodein: DKodein,
	parentContext: RaptorContext
) : KtorServerContext, RaptorContext by parentContext {

	override fun createScope() =
		KtorServerScopeImpl(context = this)


	override fun createTransaction() =
		KtorServerTransactionImpl(parentContext = this)
}
