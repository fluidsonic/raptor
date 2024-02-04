package io.fluidsonic.raptor.event

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal fun <T> Flow<T>.startIn(scope: CoroutineScope, action: suspend (T) -> Unit): Job =
	scope.launch(start = CoroutineStart.UNDISPATCHED) {
		collect(action)
	}
