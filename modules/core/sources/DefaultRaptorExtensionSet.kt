package io.fluidsonic.raptor


internal class DefaultRaptorExtensionSet : RaptorExtensionSet {

	private val values = mutableMapOf<RaptorExtensionKey<*>, Any>()


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> get(key: RaptorExtensionKey<Value>): Value? =
		values[key] as Value?


	override fun <Value : Any> set(key: RaptorExtensionKey<Value>, value: Value) {
		values.putIfAbsent(key, value)?.let { existingValue ->
			error(
				"Cannot assign value to extension key '$key' as one has already been assigned.\n" +
					"\tExisting value: $existingValue\n" +
					"\tValue to be assigned: $value"
			)
		}
	}
}
