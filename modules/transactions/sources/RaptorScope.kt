package io.fluidsonic.raptor


interface RaptorScope {

	val context: RaptorContext


	companion object
}


inline fun <Result> RaptorScope.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	with(context.createTransaction().context) {
		block()
	}
