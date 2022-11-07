package io.fluidsonic.raptor.domain


public interface RaptorEntity<out Id : RaptorEntityId> {

	public val id: Id
}
