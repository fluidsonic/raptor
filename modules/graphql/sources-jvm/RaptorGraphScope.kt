package io.fluidsonic.raptor


@RaptorDsl // FIXME is @RaptorDsl ok? needed in RaptorGraphOperation.execute() to hide define {}
public interface RaptorGraphScope : RaptorTransactionScope {

	@RaptorDsl
	override val context: RaptorGraphContext
}
