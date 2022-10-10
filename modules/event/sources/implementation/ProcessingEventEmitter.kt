package io.fluidsonic.raptor.event


public class ProcessingEventEmitter(
	private val processor: RaptorEventProcessor,
) : RaptorEventEmitter {

	override suspend fun emit(event: RaptorEvent) {
		processor.process(event)
	}
}
