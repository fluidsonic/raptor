package io.fluidsonic.raptor


internal class DefaultRaptorTransactionContext(
	override val parent: RaptorContext,
	override val properties: RaptorPropertySet
) : RaptorTransactionContext {

	override fun toString() =
		TODO() // FIXME
}
