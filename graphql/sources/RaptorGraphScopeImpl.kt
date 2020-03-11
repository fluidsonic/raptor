package io.fluidsonic.raptor


internal class RaptorGraphScopeImpl(
	parent: KtorServerTransactionScope
) : RaptorGraphScope, KtorServerTransactionScope by parent
