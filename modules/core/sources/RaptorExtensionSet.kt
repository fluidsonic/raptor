package io.fluidsonic.raptor


interface RaptorExtensionSet {

	operator fun <Value : Any> get(key: RaptorExtensionKey<Value>): Value?
	operator fun <Value : Any> set(key: RaptorExtensionKey<Value>, value: Value)


	companion object
}


inline fun <Value : Any> RaptorExtensionSet.getOrSet(key: RaptorExtensionKey<Value>, defaultValue: () -> Value): Value =
	get(key) ?: run {
		defaultValue().also { set(key, it) }
	}
