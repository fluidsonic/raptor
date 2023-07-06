package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal suspend fun <T> Flow<T>.startIn(scope: CoroutineScope, action: suspend (T) -> Unit): Job =
	scope.launch(start = CoroutineStart.UNDISPATCHED) {
		collect(action)
	}
