package io.fluidsonic.raptor


public interface RaptorGraphArgumentParsingScope : RaptorGraphScope {

	@RaptorDsl
	public fun invalid(details: String? = null): Nothing
}
