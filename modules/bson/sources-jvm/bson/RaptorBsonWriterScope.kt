package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*


@RaptorDsl
public interface RaptorBsonWriterScope : RaptorBsonScope {

	@RaptorDsl
	public val writer: RaptorBsonWriter
}
