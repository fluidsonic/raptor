package io.fluidsonic.raptor

import org.kodein.di.erased.*


internal class KtorServerTransactionContextImpl(
	parentContext: KtorServerContextImpl
) : KtorServerTransactionContextInternal, KtorServerContext by parentContext {

	override val dkodein = parentContext.createTransactionKodein {
		bind<KtorServerTransactionContext>() with instance(this@KtorServerTransactionContextImpl)
		bind<KtorServerTransactionScope>() with instance(this@KtorServerTransactionContextImpl)
		bind<RaptorTransactionContext>() with instance(this@KtorServerTransactionContextImpl)
		bind<RaptorTransactionScope>() with instance(this@KtorServerTransactionContextImpl)
	}


	override val context
		get() = this
}
