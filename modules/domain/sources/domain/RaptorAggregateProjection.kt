package io.fluidsonic.raptor.domain


public interface RaptorAggregateProjection<out Id : RaptorAggregateProjectionId> : RaptorProjection<Id> {

	public override val id: Id
}
