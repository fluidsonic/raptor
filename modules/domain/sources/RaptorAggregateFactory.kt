package io.fluidsonic.raptor.domain


public fun interface RaptorAggregateFactory<out Aggregate : RaptorAggregate<Id, *, *>, Id : RaptorAggregateId> {

	public fun create(id: Id): Aggregate
}
