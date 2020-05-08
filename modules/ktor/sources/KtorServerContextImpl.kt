package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


internal class KtorServerContextImpl(
	kodeinModule: Kodein.Module,
	parentContext: RaptorContext
) : KtorServerContext, RaptorContext by parentContext {

	override val dkodein = Kodein.direct(allowSilentOverride = true) {
		extend(parentContext.dkodein)
		import(kodeinModule)

		bind<KtorServerContext>() with instance(this@KtorServerContextImpl)
		bind<KtorServerScope>() with instance(this@KtorServerContextImpl)
	}


	override val context
		get() = this


	override fun createTransaction() =
		KtorServerTransactionImpl(parentContext = this)
}
