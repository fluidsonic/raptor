package io.fluidsonic.raptor.cqrs


public interface RaptorProjector<
	out Projection : RaptorProjection<*>,
	AggregateId : RaptorAggregateId,
	in AggregateEvent : RaptorAggregateEvent<AggregateId>,
	> {

	public fun project(events: List<RaptorEvent<AggregateId, AggregateEvent>>): Projection?


	public interface Incremental<
		out Projection : RaptorProjection<*>,
		AggregateId : RaptorAggregateId,
		in AggregateEvent : RaptorAggregateEvent<AggregateId>,
		> {

		public val projection: Projection?

		public fun add(event: RaptorEvent<AggregateId, AggregateEvent>): Projection?
	}
}
