package io.fluidsonic.raptor


interface KtorRouteTransactionScope : KtorServerTransactionScope {

	override val context: KtorRouteTransactionContext
}
