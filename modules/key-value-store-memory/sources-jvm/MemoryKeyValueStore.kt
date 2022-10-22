package io.fluidsonic.raptor.keyvaluestore.memory

import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.keyvaluestore.KeyValueStore.*
import java.util.concurrent.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow


internal class MemoryKeyValueStore<Key : Any, Value : Any> : KeyValueStore<Key, Value> {

	private val valuesByKey = ConcurrentHashMap<Key, Value>()


	override suspend fun clear() {
		valuesByKey.clear()
	}


	override fun entries(): Flow<Entry<Key, Value>> =
		valuesByKey.entries
			.asFlow()
			.map { (key, value) -> Entry(key, value) }


	override fun keys(): Flow<Key> =
		valuesByKey.keys.asFlow()


	override fun values(): Flow<Value> =
		valuesByKey.values.asFlow()


	override suspend fun set(key: Key, value: Value) {
		valuesByKey[key] = value
	}


	override suspend fun remove(key: Key) {
		valuesByKey.remove(key)
	}


	override suspend fun get(key: Key): Value? =
		valuesByKey[key]
}
