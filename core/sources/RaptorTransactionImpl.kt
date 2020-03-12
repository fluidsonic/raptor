package io.fluidsonic.raptor


internal class RaptorTransactionImpl(
	parentContext: RaptorContextImpl
) : RaptorTransaction {

	override val context = RaptorTransactionContextImpl(
		dkodein = parentContext.dkodein, // FIXME create own
		parentContext = parentContext
	)
}
