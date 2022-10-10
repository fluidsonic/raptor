package io.fluidsonic.raptor.event


public interface RaptorEventProcessor {

	public suspend fun process(event: RaptorEvent)
}
