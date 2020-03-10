package io.fluidsonic.raptor


interface RaptorKtorServerScope : RaptorScope {

	// FIXME move as this will bleed into every scope
	override fun beginTransaction(): RaptorKtorServerTransaction
}
