package io.fluidsonic.raptor


internal interface KtorServerTransactionInternal : KtorServerTransaction {

	override val context: KtorServerTransactionContextInternal


	fun createRouteTransaction(config: KtorRouteConfig): KtorRouteTransactionImpl
}
