package io.fluidsonic.raptor.keyvaluestore

import kotlin.reflect.*


public interface RaptorKeyValueStoreFactory {

	public fun <Key : Any, Value : Any> create(
		name: String,
		keyType: KType,
		valueType: KType,
	): RaptorKeyValueStore<Key, Value>


	public companion object
}


public inline fun <reified Key : Any, reified Value : Any> RaptorKeyValueStoreFactory.create(name: String): RaptorKeyValueStore<Key, Value> =
	create(name = name, keyType = typeOf<Key>(), valueType = typeOf<Value>())
