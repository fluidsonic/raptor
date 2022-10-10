package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateEventFactory { // FIXME RN

	public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> create(
		aggregateId: Id,
		change: Change,
		version: Int,
	): RaptorAggregateEvent<Id, Change>
}
