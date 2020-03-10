package io.fluidsonic.raptor


internal class RaptorGraphScopeImpl(
	parent: RaptorKtorServerTransactionScope
) : RaptorGraphScope, RaptorKtorServerTransactionScope by parent
