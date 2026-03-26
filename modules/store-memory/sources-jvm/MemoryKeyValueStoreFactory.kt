package io.fluidsonic.raptor.store.memory

import io.fluidsonic.raptor.store.*
import kotlin.reflect.*


private object MemoryKeyValueStoreFactory : RaptorKeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyClass: KClass<Key>, valueClass: KClass<Value>): RaptorKeyValueStore<Key, Value> =
		MemoryKeyValueStore()
}


public fun RaptorKeyValueStoreFactory.Companion.memory(): RaptorKeyValueStoreFactory =
	MemoryKeyValueStoreFactory
