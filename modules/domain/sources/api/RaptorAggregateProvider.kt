package io.fluidsonic.raptor.domain


public interface RaptorAggregateProvider {

	public suspend fun <Id : RaptorAggregateId> provide(
		id: Id,
	): Pair<RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>>, Int>
}
