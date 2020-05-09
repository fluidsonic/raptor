package io.fluidsonic.raptor


internal class DefaultRaptorPropertyRegistry : RaptorPropertyRegistry {

	private val valuesByKey: MutableMap<RaptorPropertyKey<*>, Any> = hashMapOf()


	override fun <Value : Any> register(key: RaptorPropertyKey<Value>, value: Value) {
		valuesByKey.putIfAbsent(key, value)?.let { existingValue ->
			error(
				"Cannot assign value to key '$key' as one has already been assigned.\n" +
					"\tExisting value: $existingValue\n" +
					"\tValue to be assigned: $value"
			)
		}
	}


	fun toPropertySet() =
		DefaultRaptorPropertySet(valuesByKey = valuesByKey.toMap())


	override fun toString() = buildString {
		append("[property registry] ->")

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
