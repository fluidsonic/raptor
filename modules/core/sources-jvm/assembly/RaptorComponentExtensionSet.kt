package io.fluidsonic.raptor


public interface RaptorComponentExtensionSet {

	public operator fun <Value : Any> get(key: RaptorComponentExtensionKey<out Value>): Value?
	public operator fun <Value : Any> set(key: RaptorComponentExtensionKey<in Value>, value: Value)


	public companion object {

		public fun default(): RaptorComponentExtensionSet =
			DefaultComponentExtensionSet()
	}
}


public inline fun <Value : Any> RaptorComponentExtensionSet.getOrSet(key: RaptorComponentExtensionKey<Value>, defaultValue: () -> Value): Value =
	get(key) ?: run {
		defaultValue().also { set(key, it) }
	}
