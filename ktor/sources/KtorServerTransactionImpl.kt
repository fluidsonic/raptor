package io.fluidsonic.raptor


internal class KtorServerTransactionImpl(
	parentContext: KtorServerContextImpl
) : KtorServerTransactionInternal {

	override val context = KtorServerTransactionContextImpl(
		parentContext = parentContext
	)


	override fun createRouteTransaction(config: KtorRouteConfig) =
		KtorRouteTransactionImpl(
			config = config,
			parent = this
		)
}
