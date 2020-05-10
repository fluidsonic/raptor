package io.fluidsonic.raptor


internal class DefaultKtorRouteTransactionContext(
	parent: KtorServerTransactionContextInternal
) : KtorRouteTransactionContext, KtorServerTransactionContextInternal by parent.context {

	override val context
		get() = this
}
