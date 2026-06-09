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

	/**
	 * Atomically read-modify-writes the entry for [key].
	 *
	 * Reads the current value (`null` if absent), passes it to [decide], and applies the
	 * returned [UpdateDecision]:
	 *
	 * - [UpdateDecision.Update] stores its value, replacing the current value or inserting it
	 *   if the entry was absent.
	 * - [UpdateDecision.Keep] leaves the entry unchanged and performs no write.
	 * - [UpdateDecision.Remove] removes the entry.
	 *
	 * The read and the write happen as one atomic step with respect to other writers. A backend
	 * that cannot do so in a single operation falls back to optimistic concurrency: on a
	 * concurrent change it re-reads the now-current value and invokes [decide] again, retrying
	 * up to [maxAttempts] times before throwing [RaptorOptimisticUpdateException]. [decide] may
	 * therefore be invoked more than once and must be a pure function of its argument — it must
	 * not produce side effects or access this store.
	 *
	 * @param maxAttempts the maximum number of times [decide] is invoked before giving up on a
	 *  backend that uses optimistic concurrency; must be at least `1`.
	 * @return the value now associated with [key]: the stored value on [UpdateDecision.Update],
	 *  `null` on [UpdateDecision.Remove], or the unchanged current value (possibly `null`) on
	 *  [UpdateDecision.Keep].
	 * @throws RaptorOptimisticUpdateException if the decision could not be applied within
	 *  [maxAttempts] attempts because the value kept changing concurrently.
	 */
	public suspend fun update(
		key: Key,
		maxAttempts: Int = 10,
		decide: (current: Value?) -> UpdateDecision<Value>,
	): Value?

	/** The action an [update] callback decides to take for the current value. */
	public sealed interface UpdateDecision<out Value : Any> {

		/** Store [value], replacing the current value or inserting it if the entry is absent. */
		public data class Update<out Value : Any> public constructor(public val value: Value) : UpdateDecision<Value>

		/** Leave the entry unchanged and stop. */
		public data object Keep : UpdateDecision<Nothing>

		/** Remove the entry. */
		public data object Remove : UpdateDecision<Nothing>
	}


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
