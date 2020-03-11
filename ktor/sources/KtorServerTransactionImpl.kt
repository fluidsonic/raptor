package io.fluidsonic.raptor

import kotlinx.atomicfu.*


internal class KtorServerTransactionImpl(
	private val parentScope: KtorServerScope
) : KtorServerTransaction {

	private val isCompleteRef = atomic(false)


	override fun complete() {
		check(isCompleteRef.compareAndSet(expect = false, update = true)) { "Transaction is already complete." }
	}


	override val isComplete
		get() = isCompleteRef.value
}
