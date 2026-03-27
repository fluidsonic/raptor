package io.fluidsonic.raptor.store

import kotlin.reflect.*


/**
 * Creates named [RaptorKeyValueStore] instances.
 */
public interface RaptorKeyValueStoreFactory {

	/** Creates a [RaptorKeyValueStore] with the given [name], using [keyClass] and [valueClass] for serialization. */
	public fun <Key : Any, Value : Any> create(
		name: String,
		keyClass: KClass<Key>,
		valueClass: KClass<Value>,
	): RaptorKeyValueStore<Key, Value>

	public companion object
}


/** Creates a [RaptorKeyValueStore] with the given [name], using reified type parameters for key and value classes. */
public inline fun <reified Key : Any, reified Value : Any> RaptorKeyValueStoreFactory.create(name: String): RaptorKeyValueStore<Key, Value> =
	create(name = name, keyClass = Key::class, valueClass = Value::class)
