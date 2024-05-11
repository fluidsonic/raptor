package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.transactions.*


public interface RaptorGraphExceptionHandlerContext : RaptorTransactionContext {

	public val graphPath: String?
}
