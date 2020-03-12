package io.fluidsonic.raptor


interface RaptorTransactionScope : RaptorScope {

	override val context: RaptorTransactionContext
}
