package io.fluidsonic.raptor

import org.kodein.di.erased.*


internal class RaptorTransactionContextImpl(
	parentContext: RaptorContextImpl
) : RaptorTransactionContext, RaptorContext by parentContext {

	override val dkodein = parentContext.createTransactionKodein {
		bind<RaptorTransactionContext>() with instance(this@RaptorTransactionContextImpl)
		bind<RaptorTransactionScope>() with instance(this@RaptorTransactionContextImpl)
	}


	override val context
		get() = this
}
