package io.fluidsonic.raptor


internal class DefaultKtorServerContext(
	parentContext: RaptorContext
) : KtorServerContext, RaptorContext by parentContext {

	override val context
		get() = this


	override fun createTransaction() =
		DefaultKtorServerTransaction(parentContext = this)
}
