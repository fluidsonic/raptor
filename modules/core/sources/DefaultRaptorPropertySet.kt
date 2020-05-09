package io.fluidsonic.raptor


internal class DefaultRaptorPropertySet(
	private val valuesByKey: Map<RaptorPropertyKey<*>, Any>
) {

	@Suppress("UNCHECKED_CAST")
	operator fun <Value : Any> get(key: RaptorPropertyKey<Value>): Value? =
		valuesByKey[key] as Value?


	fun isEmpty() =
		valuesByKey.isEmpty()


	override fun toString() = buildString {
		append("[property set] ->")

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
