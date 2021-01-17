package io.fluidsonic.raptor

import kotlinx.coroutines.*


public interface RaptorLifecycle : CoroutineScope {

	public val state: State

	public suspend fun startIn(scope: CoroutineScope)
	public suspend fun stop()


	public enum class State {

		started,
		starting,
		stopped,
		stopping
	}


	public companion object
}
