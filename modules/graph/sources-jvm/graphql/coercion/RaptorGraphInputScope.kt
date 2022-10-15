package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


public interface RaptorGraphInputScope : RaptorTransactionScope {

	// FIXME We can throw a special exception that the graph system catches to add contextual information.
	//       That way we can support it deep inside code and still have that information.
	@RaptorDsl
	public fun invalid(details: String? = null): Nothing
}
