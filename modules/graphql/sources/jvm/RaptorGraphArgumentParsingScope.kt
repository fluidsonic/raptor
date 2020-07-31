package io.fluidsonic.raptor


public interface RaptorGraphArgumentParsingScope : RaptorGraphScope {

	// FIXME We can throw a special exception that the graph system catches to add contextual information.
	//       That way we can support it deep inside code and still have that information.
	@RaptorDsl
	public fun invalid(details: String? = null): Nothing
}
