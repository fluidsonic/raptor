package io.fluidsonic.raptor


internal class DefaultRaptorTransaction(
	override val context: DefaultRaptorTransactionContext,
) : RaptorTransaction {

	// FIXME rework
	object PropertyKey : RaptorPropertyKey<RaptorTransaction> {

		override fun toString() = "transaction"
	}
}
