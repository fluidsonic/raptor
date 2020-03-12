package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext {

	override fun createScope(): RaptorTransactionScope
}
