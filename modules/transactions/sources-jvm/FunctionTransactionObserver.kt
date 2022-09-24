package io.fluidsonic.raptor.transactions


internal class FunctionTransactionObserver(
	private val onFail: (suspend RaptorTransactionContext.(error: Throwable) -> Unit)? = null,
	private val onStart: (suspend RaptorTransactionContext.() -> Unit)? = null,
	private val onStop: (suspend RaptorTransactionContext.() -> Unit)? = null,
) : RaptorTransactionObserver {

	override suspend fun onFail(context: RaptorTransactionContext, error: Throwable) {
		onFail?.invoke(context, error)
	}


	override suspend fun onStart(context: RaptorTransactionContext) {
		onStart?.invoke(context)
	}


	override suspend fun onStop(context: RaptorTransactionContext) {
		onStart?.invoke(context)
	}
}


internal fun RaptorTransactionObserver(
	onFail: suspend RaptorTransactionContext.(error: Throwable) -> Unit,
	onStart: suspend RaptorTransactionContext.() -> Unit,
	onStop: suspend RaptorTransactionContext.() -> Unit,
): RaptorTransactionObserver =
	FunctionTransactionObserver(onFail = onFail, onStart = onStart, onStop = onStop)


@JvmName("RaptorTransactionObserverOrNull")
@Suppress("FunctionName")
internal fun RaptorTransactionObserver(
	onFail: (suspend RaptorTransactionContext.(error: Throwable) -> Unit)?,
	onStart: (suspend RaptorTransactionContext.() -> Unit)?,
	onStop: (suspend RaptorTransactionContext.() -> Unit)?,
): RaptorTransactionObserver? =
	when {
		onFail != null || onStart != null || onStop != null -> FunctionTransactionObserver(onFail = onFail, onStart = onStart, onStop = onStop)
		else -> null
	}
