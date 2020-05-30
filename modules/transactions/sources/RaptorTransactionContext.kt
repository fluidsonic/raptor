package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext, RaptorTransactionScope {

	override fun toString(): String


	override fun asScope(): RaptorTransactionScope =
		this


	override val context: RaptorTransactionContext
		get() = this


	companion object
}
