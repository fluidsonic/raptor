package io.fluidsonic.raptor


interface KtorServerContext : RaptorContext {

	override fun createScope(): KtorServerScope
	override fun createTransaction(): KtorServerTransaction
}
