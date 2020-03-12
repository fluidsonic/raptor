package io.fluidsonic.raptor


interface KtorServerTransactionScope : KtorServerScope, RaptorTransactionScope {

	override val context: KtorServerTransactionContext
}
