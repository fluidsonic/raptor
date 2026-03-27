package io.fluidsonic.raptor.store

import kotlin.reflect.*


/**
 * Creates named [RaptorLogStore] instances.
 */
public interface RaptorLogStoreFactory {

	/** Creates a [RaptorLogStore] with the given [name], using [valueClass] for serialization. */
	public fun <Value : Any> create(
		name: String,
		valueClass: KClass<Value>,
	): RaptorLogStore<Value>

	public companion object
}


/** Creates a [RaptorLogStore] with the given [name], using a reified type parameter for the value class. */
public inline fun <reified Value : Any> RaptorLogStoreFactory.create(name: String): RaptorLogStore<Value> =
	create(name = name, valueClass = Value::class)
