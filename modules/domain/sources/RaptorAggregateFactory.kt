package io.fluidsonic.raptor.cqrs


public fun interface RaptorAggregateFactory<out Aggregate : RaptorAggregate<Id, *, *>, Id : RaptorAggregateId> {

	public fun create(id: Id): Aggregate
}
