package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext, RaptorTransactionScope {

	override val context: RaptorTransactionContext
		get() = this


	override fun toString(): String


	companion object
}
