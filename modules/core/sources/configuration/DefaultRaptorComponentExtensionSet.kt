package io.fluidsonic.raptor


internal class DefaultRaptorComponentExtensionSet : RaptorComponentExtensionSet {

	private val valuesByKey: MutableMap<RaptorComponentExtensionKey<*>, Any> = mutableMapOf()


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> get(key: RaptorComponentExtensionKey<out Value>): Value? =
		valuesByKey[key] as Value?


	override fun <Value : Any> set(key: RaptorComponentExtensionKey<in Value>, value: Value) {
		valuesByKey.putIfAbsent(key, value)?.let { existingValue ->
			error(
				"Cannot assign value to extension key '$key' as one has already been assigned.\n" +
					"\tExisting value: $existingValue\n" +
					"\tValue to be assigned: $value"
			)
		}
	}


	override fun toString(): String = buildString {
		val valuesByKey = valuesByKey
			.filterKeys { key ->
				when (key) {
					RaptorComponentRegistryExtensionKey ->
						false

					else ->
						true
				}
			}
			.map { (key, value) -> key.toString() to value.toString() }
			.sortedBy { (key) -> key }
			.ifEmpty { return@buildString }

		valuesByKey.forEachIndexed { index, (key, value) ->
			if (index > 0)
				append("\n")

			append("[$key]".prependIndent("\t").trimStart())
			append(" ->")

			if (value.contains('\n')) {
				append("\n")
				append(value.prependIndent("\t"))
			}
			else {
				append(" ")
				append(value)
			}
		}
	}
}
