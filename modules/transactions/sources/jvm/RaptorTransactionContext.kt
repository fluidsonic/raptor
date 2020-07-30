package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext, RaptorTransactionScope {

	override fun toString(): String


	override fun asScope(): RaptorTransactionScope =
		this


	override val context: RaptorTransactionContext
		get() = this


	override val parent: RaptorContext


	companion object


	interface Lazy : RaptorTransactionContext, RaptorContext.Lazy
}
