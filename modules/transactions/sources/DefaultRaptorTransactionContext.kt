package io.fluidsonic.raptor


internal class DefaultRaptorTransactionContext(
	parentContext: DefaultRaptorContext
) : RaptorTransactionContext, RaptorContext by parentContext {

	override val context
		get() = this
}
