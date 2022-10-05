package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateProjector<
	out Projection : RaptorAggregateProjection<Id>,
	Id : RaptorAggregateProjectionId,
	in AggregateEvent : RaptorAggregateEvent<Id>,
	> {

	public fun project(events: List<RaptorEvent<Id, AggregateEvent>>): Projection?


	public interface Incremental<
		out Projection : RaptorAggregateProjection<Id>,
		Id : RaptorAggregateProjectionId,
		in AggregateEvent : RaptorAggregateEvent<Id>,
		> {

		public val projection: Projection?

		public fun add(event: RaptorEvent<Id, AggregateEvent>): Projection?
	}
}
