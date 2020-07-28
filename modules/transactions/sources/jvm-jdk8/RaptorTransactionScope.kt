package io.fluidsonic.raptor


interface RaptorTransactionScope : RaptorScope {

	@RaptorDsl
	override val context: RaptorTransactionContext
}
