package io.fluidsonic.raptor


internal class RaptorGraphScopeImpl(
	parent: RaptorTransactionScope
) : RaptorGraphScope, RaptorTransactionScope by parent
