package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorBsonReaderScope : RaptorBsonScope {

	@RaptorDsl
	public fun invalidValue(message: String): Nothing

	@RaptorDsl
	public val reader: RaptorBsonReader

	@RaptorDsl
	public fun missingFieldValue(name: String): Nothing
}
