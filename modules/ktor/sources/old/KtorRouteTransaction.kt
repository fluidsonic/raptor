package io.fluidsonic.raptor


interface KtorRouteTransaction : KtorServerTransaction {

	override val context: KtorRouteTransactionContext
}
