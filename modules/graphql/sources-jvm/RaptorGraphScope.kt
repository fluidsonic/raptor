package io.fluidsonic.raptor

import io.fluidsonic.raptor.transactions.*


@RaptorDsl // TODO is @RaptorDsl ok? needed in RaptorGraphOperation.execute() to hide define {}
public interface RaptorGraphScope : RaptorTransactionScope {

	@RaptorDsl
	override val context: RaptorGraphContext
}
