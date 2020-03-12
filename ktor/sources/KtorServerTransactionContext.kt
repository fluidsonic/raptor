package io.fluidsonic.raptor


interface KtorServerTransactionContext : KtorServerContext, RaptorTransactionContext {

	override fun createScope(): KtorServerTransactionScope
}
