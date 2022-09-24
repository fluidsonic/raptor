package io.fluidsonic.raptor.transactions


public interface RaptorTransaction {

	public val context: RaptorTransactionContext

	public suspend fun fail(error: Throwable)
	public suspend fun start()
	public suspend fun stop()
}
