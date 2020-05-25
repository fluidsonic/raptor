package io.fluidsonic.raptor


internal class RaptorGraphScopeImpl(
	context: RaptorTransactionContext
) : RaptorGraphScope, RaptorTransactionScope by context
