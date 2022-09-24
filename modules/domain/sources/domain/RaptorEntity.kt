package io.fluidsonic.raptor.cqrs


public interface RaptorEntity<out Id : RaptorEntityId> {

	public val id: Id
}
