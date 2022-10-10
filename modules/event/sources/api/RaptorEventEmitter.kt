package io.fluidsonic.raptor.event


public interface RaptorEventEmitter {

	public suspend fun emit(event: RaptorEvent)
}
