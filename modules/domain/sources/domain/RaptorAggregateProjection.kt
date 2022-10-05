package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateProjection<out Id : RaptorAggregateProjectionId> : RaptorProjection<Id> {

	public override val id: Id
}
