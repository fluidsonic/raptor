package io.fluidsonic.raptor.store


/**
 * An append-only log store.
 */
public interface RaptorLogStore<in Value : Any> {

	/** Appends [value] to the log. */
	public suspend fun append(value: Value)

	public companion object
}
