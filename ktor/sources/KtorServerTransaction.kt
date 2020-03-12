package io.fluidsonic.raptor


interface KtorServerTransaction : RaptorTransaction {

	override val context: KtorServerTransactionContext
}
