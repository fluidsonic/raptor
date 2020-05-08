package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


internal class KtorRouteTransactionContextImpl(
	config: KtorRouteConfig,
	parent: KtorServerTransactionContextInternal
) : KtorRouteTransactionContext, KtorServerTransactionContextInternal by parent.context {

	override val dkodein = Kodein.direct(allowSilentOverride = true) {
		extend(parent.context.dkodein)
		import(config.kodeinModule)

		bind<KtorRouteTransactionContext>() with instance(this@KtorRouteTransactionContextImpl)
		bind<KtorRouteTransactionScope>() with instance(this@KtorRouteTransactionContextImpl)
		bind<KtorServerTransactionContext>() with instance(this@KtorRouteTransactionContextImpl)
		bind<KtorServerTransactionScope>() with instance(this@KtorRouteTransactionContextImpl)
		bind<RaptorTransactionContext>() with instance(this@KtorRouteTransactionContextImpl)
		bind<RaptorTransactionScope>() with instance(this@KtorRouteTransactionContextImpl)
	}


	override val context
		get() = this
}
