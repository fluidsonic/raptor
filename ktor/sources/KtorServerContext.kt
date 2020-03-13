package io.fluidsonic.raptor


interface KtorServerContext : RaptorContext, KtorServerScope {

	override fun createTransaction(): KtorServerTransaction
}
