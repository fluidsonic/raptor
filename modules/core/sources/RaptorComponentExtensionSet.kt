package io.fluidsonic.raptor


interface RaptorComponentExtensionSet {

	operator fun <Value : Any> get(key: RaptorComponentExtensionKey<Value>): Value?
	operator fun <Value : Any> set(key: RaptorComponentExtensionKey<Value>, value: Value)


	companion object
}


inline fun <Value : Any> RaptorComponentExtensionSet.getOrSet(key: RaptorComponentExtensionKey<Value>, defaultValue: () -> Value): Value =
	get(key) ?: run {
		defaultValue().also { set(key, it) }
	}
