package io.fluidsonic.raptor.transactions


internal interface RaptorTransactionObserver {

	suspend fun onFail(context: RaptorTransactionContext, error: Throwable)
	suspend fun onStart(context: RaptorTransactionContext)
	suspend fun onStop(context: RaptorTransactionContext)
}
