package io.fluidsonic.raptor


internal class DefaultRaptorTransaction(
	parentContext: DefaultRaptorContext
) : RaptorTransaction {

	override val context = DefaultRaptorTransactionContext(
		parentContext = parentContext
	)
}
