package io.fluidsonic.raptor


internal class DefaultRaptorContext(
	override val properties: RaptorPropertySet
) : RaptorContext {

	override val parent: RaptorContext?
		get() = null


	override fun toString(): String = buildString {
		append("[context] ->")

		if (context.properties.isEmpty()) {
			append(" (empty)")
			return@buildString
		}

		append("\n")
		append(context.properties.toString().prependIndent("\t"))
	}
}
