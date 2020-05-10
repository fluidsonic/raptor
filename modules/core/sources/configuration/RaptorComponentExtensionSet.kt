package io.fluidsonic.raptor


interface RaptorComponentExtensionSet {

	operator fun <Value : Any> get(key: RaptorComponentExtensionKey<out Value>): Value?
	operator fun <Value : Any> set(key: RaptorComponentExtensionKey<in Value>, value: Value)


	companion object {

		fun default(): RaptorComponentExtensionSet =
			DefaultRaptorComponentExtensionSet()
	}
}


inline fun <Value : Any> RaptorComponentExtensionSet.getOrSet(key: RaptorComponentExtensionKey<Value>, defaultValue: () -> Value): Value =
	get(key) ?: run {
		defaultValue().also { set(key, it) }
	}
