package io.fluidsonic.raptor


internal class DefaultRaptorKeyValueRegistry(private val elementName: String) : RaptorKeyValueRegistry {

	private val valuesByKey: MutableMap<RaptorKey<*>, Any> = hashMapOf()


	override fun <Value : Any> register(key: RaptorKey<in Value>, value: Value) {
		valuesByKey.putIfAbsent(key, value)?.let { existingValue ->
			error(
				"Cannot assign value to key '$key' as one has already been assigned.\n" +
					"\tExisting value: $existingValue\n" +
					"\tValue to be assigned: $value"
			)
		}
	}


	override fun toSet() =
		DefaultRaptorKeyValueSet(elementName = elementName, valuesByKey = valuesByKey.toMap())


	override fun toString(): String = buildString {
		append("[$elementName registry] ->")

		if (valuesByKey.isEmpty()) {
			append(" (empty)")
			return@buildString
		}

		append("\n")
		valuesByKey.entries
			.map { (key, value) -> key.toString() to value.toString() }
			.sortedBy { (key) -> key }
			.forEachIndexed { index, (key, value) ->
				if (index > 0)
					append("\n")

				append("[$key]".prependIndent("\t"))
				append(" ->")

				if (value.contains("\n")) {
					append("\n")
					append(value.prependIndent("\t\t"))
				}
				else {
					append(" ")
					append(value)
				}
			}
	}
}
