package io.fluidsonic.raptor


internal class DefaultKtorServerTransaction(
	parentContext: DefaultKtorServerContext
) : KtorServerTransactionInternal {

	override val context = DefaultKtorServerTransactionContext(
		parentContext = parentContext
	)
}
