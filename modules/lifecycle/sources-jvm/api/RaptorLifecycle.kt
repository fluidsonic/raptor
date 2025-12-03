package io.fluidsonic.raptor.lifecycle

import kotlinx.coroutines.*


public interface RaptorLifecycle : CoroutineScope {

	public val state: State

	public suspend fun startIn(scope: CoroutineScope) // FIXME scope + suspending = not good DX
	public suspend fun stop()


	public enum class State {

		started,
		starting,
		stopped,
		stopping
	}
}
