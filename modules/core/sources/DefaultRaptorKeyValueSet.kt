package io.fluidsonic.raptor


internal class DefaultRaptorKeyValueSet(
	private val elementName: String,
	private val valuesByKey: Map<RaptorKey<*>, Any>
) : RaptorKeyValueSet {

	@Suppress("UNCHECKED_CAST")
	override operator fun <Value : Any> get(key: RaptorKey<out Value>): Value? =
		valuesByKey[key] as Value?


	override fun isEmpty() =
		valuesByKey.isEmpty()


	override fun toString() = buildString {
		append("[$elementName set] ->")

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
