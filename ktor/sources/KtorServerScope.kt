package io.fluidsonic.raptor


interface KtorServerScope : RaptorScope {

	// FIXME move as this will bleed into every scope
	override fun beginTransaction(): KtorServerTransaction
}
