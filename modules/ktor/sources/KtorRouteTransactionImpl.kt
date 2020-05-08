package io.fluidsonic.raptor


internal class KtorRouteTransactionImpl(
	config: KtorRouteConfig,
	parent: KtorServerTransactionInternal
) : KtorRouteTransaction, KtorServerTransactionInternal {

	override val context = KtorRouteTransactionContextImpl(
		config = config,
		parent = parent.context
	)
}
