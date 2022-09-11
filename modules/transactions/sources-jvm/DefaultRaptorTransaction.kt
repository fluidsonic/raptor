package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


internal class DefaultRaptorTransaction(
	override val context: DefaultRaptorTransactionContext,
) : RaptorTransaction {

	object PropertyKey : RaptorPropertyKey<RaptorTransaction> {

		override fun toString() = "transaction"
	}
}
