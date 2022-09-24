package io.fluidsonic.raptor.cqrs


public interface RaptorEventFactory {

	public fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorEvent<Id, Event>
}
