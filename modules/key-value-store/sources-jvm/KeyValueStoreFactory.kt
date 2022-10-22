package io.fluidsonic.raptor.keyvaluestore

import kotlin.reflect.*


public interface KeyValueStoreFactory {

	public fun <Key : Any, Value : Any> create(
		name: String,
		keyClass: KClass<Key>,
		valueClass: KClass<Value>,
	): KeyValueStore<Key, Value>


	public companion object
}


public inline fun <reified Key : Any, reified Value : Any> KeyValueStoreFactory.create(name: String): KeyValueStore<Key, Value> =
	create(name = name, keyClass = Key::class, valueClass = Value::class)
