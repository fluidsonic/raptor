package io.fluidsonic.raptor.di


@Suppress("UNCHECKED_CAST")
internal inline fun <K, V> MutableMap<K, V>.getOrPutNullable(key: K, defaultValue: () -> V): V {
	val value = get(key)

	return when {
		value == null && !containsKey(key) -> defaultValue().also { put(key, it) }
		else -> value as V
	}
}
