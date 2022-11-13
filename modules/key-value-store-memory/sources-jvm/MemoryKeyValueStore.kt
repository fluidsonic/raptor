package io.fluidsonic.raptor.keyvaluestore.memory

import io.fluidsonic.raptor.keyvaluestore.*
import java.util.concurrent.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow


internal class MemoryKeyValueStore<Key : Any, Value : Any> : RaptorKeyValueStore<Key, Value> {

	private val valuesByKey = ConcurrentHashMap<Key, Value>()


	override suspend fun clear() {
		valuesByKey.clear()
	}


	override fun entries(): Flow<Pair<Key, Value>> =
		valuesByKey.entries
			.asFlow()
			.map { it.toPair() }


	override fun keys(): Flow<Key> =
		valuesByKey.keys.asFlow()


	override fun values(): Flow<Value> =
		valuesByKey.values.asFlow()


	override suspend fun set(key: Key, value: Value) {
		valuesByKey[key] = value
	}


	override suspend fun setIfAbsent(key: Key, value: Value): Boolean =
		valuesByKey.putIfAbsent(key, value) == null


	override suspend fun remove(key: Key): Boolean =
		valuesByKey.remove(key) != null


	override suspend fun get(key: Key): Value? =
		valuesByKey[key]
}
