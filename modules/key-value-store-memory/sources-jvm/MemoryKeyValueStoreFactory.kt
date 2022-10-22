package io.fluidsonic.raptor.keyvaluestore.memory

import io.fluidsonic.raptor.keyvaluestore.*
import kotlin.reflect.*


private object MemoryKeyValueStoreFactory : KeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyClass: KClass<Key>, valueClass: KClass<Value>): KeyValueStore<Key, Value> =
		MemoryKeyValueStore()
}


public fun KeyValueStoreFactory.Companion.memory(): KeyValueStoreFactory =
	MemoryKeyValueStoreFactory
