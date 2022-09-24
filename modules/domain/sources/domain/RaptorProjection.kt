package io.fluidsonic.raptor.cqrs


public interface RaptorProjection<out Id : RaptorProjectionId> : RaptorEntity<Id> {

	public override val id: Id
}
