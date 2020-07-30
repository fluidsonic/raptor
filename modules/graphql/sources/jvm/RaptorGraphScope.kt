package io.fluidsonic.raptor


@RaptorDsl // FIXME is this ok? needed in RaptorGraphOperation.execute() to hide define {}
interface RaptorGraphScope : RaptorTransactionScope {

	@RaptorDsl
	override val context: RaptorGraphContext
}
