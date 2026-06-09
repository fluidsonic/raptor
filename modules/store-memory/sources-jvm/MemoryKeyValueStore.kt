package io.fluidsonic.raptor.store.memory

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.RaptorKeyValueStore.*
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


	override suspend fun update(
		key: Key,
		maxAttempts: Int,
		decide: (current: Value?) -> UpdateDecision<Value>,
	): Value? =
		// ConcurrentHashMap.compute applies the read-modify-write atomically under the bin lock,
		// so the in-memory store is always conflict-free: it never retries and ignores maxAttempts.
		// Its return value is exactly the post-update value: decision.value for Update, null for
		// Remove, and the unchanged current (value or null) for Keep.
		valuesByKey.compute(key) { _, current ->
			when (val decision = decide(current)) {
				is UpdateDecision.Keep -> current
				is UpdateDecision.Remove -> null
				is UpdateDecision.Update -> decision.value
			}
		}
}
