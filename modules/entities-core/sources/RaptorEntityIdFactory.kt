package io.fluidsonic.raptor


public fun interface RaptorEntityIdFactory<out Id : RaptorEntityId> {

	public fun create(): Id
}
