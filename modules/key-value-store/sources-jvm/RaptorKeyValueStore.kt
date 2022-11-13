package io.fluidsonic.raptor.keyvaluestore

import kotlinx.coroutines.flow.*


public interface RaptorKeyValueStore<Key : Any, Value : Any> {

	public suspend fun clear()
	public fun entries(): Flow<Pair<Key, Value>>
	public fun keys(): Flow<Key>
	public fun values(): Flow<Value>
	public suspend fun get(key: Key): Value?
	public suspend fun remove(key: Key): Boolean
	public suspend fun set(key: Key, value: Value)
	public suspend fun setIfAbsent(key: Key, value: Value): Boolean
}


public suspend fun <Key : Any, Value : Any> RaptorKeyValueStore<Key, Value>.setIfAbsentOrRemove(key: Key, value: Value?) {
	when (value) {
		null -> remove(key)
		else -> setIfAbsent(key, value)
	}
}


public suspend fun <Key : Any, Value : Any> RaptorKeyValueStore<Key, Value>.setOrRemove(key: Key, value: Value?) {
	when (value) {
		null -> remove(key)
		else -> set(key, value)
	}
}
