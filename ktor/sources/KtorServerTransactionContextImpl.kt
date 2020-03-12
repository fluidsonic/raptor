package io.fluidsonic.raptor

import org.kodein.di.*


internal class KtorServerTransactionContextImpl(
	override val dkodein: DKodein,
	parentContext: KtorServerContextImpl
) : KtorServerTransactionContext, KtorServerContext by parentContext {

	override fun createScope() = KtorServerTransactionScopeImpl(context = this)
}
