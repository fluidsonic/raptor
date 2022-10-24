package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*


public interface RaptorBsonScope : RaptorScope {

	@RaptorDsl
	public val codecRegistry: RaptorBsonCodecRegistry
}
