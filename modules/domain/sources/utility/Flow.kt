package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal fun <T> Flow<T>.launchDispatchIn(scope: CoroutineScope): Job =
	scope.launch(RaptorAggregateStreamDispatch) {
		collect()
	}


internal fun <T> Flow<T>.startIn(scope: CoroutineScope, action: suspend (T) -> Unit): Job =
	scope.launch(RaptorAggregateStreamDispatch, start = CoroutineStart.UNDISPATCHED) {
		collect(action)
	}
