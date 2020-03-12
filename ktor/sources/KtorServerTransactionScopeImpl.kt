package io.fluidsonic.raptor


internal class KtorServerTransactionScopeImpl(
	override val context: KtorServerTransactionContextImpl
) : KtorServerTransactionScope
