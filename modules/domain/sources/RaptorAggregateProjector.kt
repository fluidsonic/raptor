package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateProjector<
	out Projection : RaptorAggregateProjection<Id>,
	Id : RaptorAggregateProjectionId,
	in AggregateEvent : RaptorAggregateChange<Id>,
	> {

	public fun project(events: List<RaptorAggregateEvent<Id, AggregateEvent>>): Projection?


	public interface Incremental<
		out Projection : RaptorAggregateProjection<Id>,
		Id : RaptorAggregateProjectionId,
		in AggregateEvent : RaptorAggregateChange<Id>,
		> {

		public val projection: Projection?

		public fun add(event: RaptorAggregateEvent<Id, AggregateEvent>): Projection?
	}
}
