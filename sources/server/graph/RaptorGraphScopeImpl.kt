package io.fluidsonic.raptor


internal class RaptorGraphScopeImpl(
	parent: RaptorServerTransactionScope
) : RaptorGraphScope, RaptorServerTransactionScope by parent
