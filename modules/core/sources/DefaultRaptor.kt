package io.fluidsonic.raptor


internal class DefaultRaptor(
	private val properties: DefaultRaptorPropertySet
) : Raptor {

	override fun <Value : Any> get(key: RaptorPropertyKey<Value>): Value? =
		properties[key]


	override fun toString() = buildString {
		append("[raptor] ->")

		if (properties.isEmpty()) {
			append(" (empty)")
			return@buildString
		}

		append("\n")
		append(properties.toString().prependIndent("\t"))
	}
}
