package io.fluidsonic.raptor.keyvaluestore

import kotlin.reflect.*


public interface RaptorKeyValueStoreFactory {

	public fun <Key : Any, Value : Any> create(
		name: String,
		keyClass: KClass<Key>,
		valueClass: KClass<Value>,
	): RaptorKeyValueStore<Key, Value>


	public companion object
}


public inline fun <reified Key : Any, reified Value : Any> RaptorKeyValueStoreFactory.create(name: String): RaptorKeyValueStore<Key, Value> =
	create(name = name, keyClass = Key::class, valueClass = Value::class)
