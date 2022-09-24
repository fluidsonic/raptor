package io.fluidsonic.raptor.cqrs


public fun interface RaptorEntityIdFactory<out Id : RaptorEntityId> {

	public fun create(): Id
}
