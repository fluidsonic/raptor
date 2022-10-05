package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateEventFactory { // FIXME RN

	public fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorEvent<Id, Event>
}
