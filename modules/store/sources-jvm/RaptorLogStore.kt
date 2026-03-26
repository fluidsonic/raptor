package io.fluidsonic.raptor.store


public interface RaptorLogStore<in Value : Any> {

	public suspend fun append(value: Value)

	public companion object
}
