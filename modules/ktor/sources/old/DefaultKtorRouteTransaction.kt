package io.fluidsonic.raptor


internal class DefaultKtorRouteTransaction(
	parent: KtorServerTransactionInternal
) : KtorRouteTransaction, KtorServerTransactionInternal {

	override val context = DefaultKtorRouteTransactionContext(
		parent = parent.context
	)
}
