package io.fluidsonic.raptor

import kotlin.coroutines.*


interface RaptorLifecycle {

	val coroutineContext: CoroutineContext
	val state: State

	suspend fun start()
	suspend fun stop()


	enum class State {

		started,
		starting,
		stopped,
		stopping
	}


	companion object
}
