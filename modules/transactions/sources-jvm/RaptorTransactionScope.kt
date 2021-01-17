package io.fluidsonic.raptor


public interface RaptorTransactionScope : RaptorScope {

	@RaptorDsl
	override val context: RaptorTransactionContext
}
