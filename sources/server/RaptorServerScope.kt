package io.fluidsonic.raptor


interface RaptorServerScope : RaptorScope {

	// FIXME move as this will bleed into every scope
	override fun beginTransaction(): RaptorServerTransaction
}
