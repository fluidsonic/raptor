package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext {

	override fun asScope(): RaptorTransactionScope
	override fun toString(): String


	companion object
}
