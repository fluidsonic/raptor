package io.fluidsonic.raptor.domain


/**
 * Doesn't support concurrent access.
 */
public interface RaptorIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> {

	public suspend fun lastEventId(): RaptorAggregateEventId?
	public suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>>
	public suspend fun reload(): List<RaptorAggregateEvent<Id, Change>>
	public suspend fun save(id: Id, events: List<RaptorAggregateEvent<Id, Change>>)


	public companion object
}
