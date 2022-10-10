package io.fluidsonic.raptor.event

import kotlinx.coroutines.flow.*


public class ParallelDispatchEventProcessor(
	private val onError: (error: Throwable, event: RaptorEvent) -> Unit, // FIXME Add error handling.
) : RaptorEventProcessor, RaptorEventSource {

	private val flow: MutableSharedFlow<RaptorEvent> = MutableSharedFlow()


	override fun asFlow(): Flow<RaptorEvent> =
		flow


	override suspend fun process(event: RaptorEvent) {
		// FIXME Test if collectors actually run in parallel. And if we actually want that.
		flow.emit(event)
	}
}
