package io.fluidsonic.raptor.event

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal suspend fun <T> Flow<T>.startIn(scope: CoroutineScope, action: suspend (T) -> Unit): Job {
	val completion = CompletableDeferred<Unit>()

	return onEach(action)
		.onStart { completion.complete(Unit) }
		.launchIn(scope)
		.also { completion.await() }
}
