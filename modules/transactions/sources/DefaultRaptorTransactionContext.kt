package io.fluidsonic.raptor


internal class DefaultRaptorTransactionContext(
	override val parent: RaptorContext,
	override val properties: RaptorPropertySet
) : RaptorTransactionContext, RaptorTransactionScope {

	override val context: RaptorTransactionContext
		get() = this


	override fun asScope(): RaptorTransactionScope =
		this


	override fun toString() =
		TODO() // FIXME
}
