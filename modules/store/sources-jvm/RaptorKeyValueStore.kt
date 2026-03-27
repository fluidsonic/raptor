package io.fluidsonic.raptor.store

import kotlinx.coroutines.flow.*


/**
 * A typed key-value store for associating keys with values.
 */
public interface RaptorKeyValueStore<Key : Any, Value : Any> {

	/** Removes all entries from the store. */
	public suspend fun clear()

	/** Returns a [Flow] of all key-value pairs in the store. */
	public fun entries(): Flow<Pair<Key, Value>>

	/** Returns a [Flow] of all keys in the store. */
	public fun keys(): Flow<Key>

	/** Returns a [Flow] of all values in the store. */
	public fun values(): Flow<Value>

	/** Returns the value associated with [key], or `null` if no such entry exists. */
	public suspend fun get(key: Key): Value?

	/** Removes the entry for [key]. Returns `true` if the entry existed. */
	public suspend fun remove(key: Key): Boolean

	/** Sets [value] for [key], replacing any existing value. */
	public suspend fun set(key: Key, value: Value)

	/** Sets [value] for [key] only if no entry exists for [key]. Returns `true` if the value was set. */
	public suspend fun setIfAbsent(key: Key, value: Value): Boolean

	public companion object
}


/** Sets [value] for [key] if absent when [value] is non-null, or removes the entry for [key] when [value] is `null`. */
public suspend fun <Key : Any, Value : Any> RaptorKeyValueStore<Key, Value>.setIfAbsentOrRemove(key: Key, value: Value?) {
	when (value) {
		null -> remove(key)
		else -> setIfAbsent(key, value)
	}
}


/** Sets [value] for [key] when [value] is non-null, or removes the entry for [key] when [value] is `null`. */
public suspend fun <Key : Any, Value : Any> RaptorKeyValueStore<Key, Value>.setOrRemove(key: Key, value: Value?) {
	when (value) {
		null -> remove(key)
		else -> set(key, value)
	}
}
