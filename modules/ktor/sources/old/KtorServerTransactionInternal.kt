package io.fluidsonic.raptor

import io.fluidsonic.raptor.internal.KtorRouteConfiguration


internal interface KtorServerTransactionInternal : KtorServerTransaction {

	override val context: KtorServerTransactionContextInternal


	fun createRouteTransaction(configuration: KtorRouteConfiguration) =
		DefaultKtorRouteTransaction(
			parent = this
		)
}
