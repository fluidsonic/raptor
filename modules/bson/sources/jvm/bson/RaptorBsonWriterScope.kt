package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorBsonWriterScope : RaptorBsonScope {

	@RaptorDsl
	public val writer: RaptorBsonWriter
}
