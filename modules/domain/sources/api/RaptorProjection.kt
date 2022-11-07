package io.fluidsonic.raptor.domain


public interface RaptorProjection<out Id : RaptorProjectionId> : RaptorEntity<Id> {

	public override val id: Id
}
