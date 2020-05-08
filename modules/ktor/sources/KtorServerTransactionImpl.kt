package io.fluidsonic.raptor


internal class KtorServerTransactionImpl(
	parentContext: KtorServerContextImpl
) : KtorServerTransactionInternal {

	override val context = KtorServerTransactionContextImpl(
		parentContext = parentContext
	)
}
