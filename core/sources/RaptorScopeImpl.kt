package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorScopeImpl(
	override val dkodein: DKodein
) : RaptorScope {

	// FIXME call onStarts (begin!), add kodein
	override fun beginTransaction() =
		RaptorTransactionImpl(parentScope = this)
}
