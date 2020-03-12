package io.fluidsonic.raptor


internal class KtorServerTransactionImpl(
	parentContext: KtorServerContextImpl
) : KtorServerTransaction {

	override val context = KtorServerTransactionContextImpl(
		dkodein = parentContext.dkodein, // FIXME create own
		parentContext = parentContext
	)
}
