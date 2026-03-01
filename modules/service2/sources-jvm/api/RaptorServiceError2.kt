package io.fluidsonic.raptor.service2

import kotlin.coroutines.*


public data class RaptorServiceError2(
	val coroutineContext: CoroutineContext,
	val throwable: Throwable,
)
