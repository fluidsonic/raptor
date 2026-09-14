package io.fluidsonic.raptor.domain


public interface RaptorAggregateStore : RaptorAggregateLoader {

	public suspend fun add(events: List<RaptorAggregateEvent<*, *>>)
	public suspend fun start() {}


	public companion object
}
