package io.fluidsonic.raptor


internal class DefaultKtorServerTransactionContext(
	parentContext: DefaultKtorServerContext
) : KtorServerTransactionContextInternal, KtorServerContext by parentContext {

	override val context
		get() = this
}
