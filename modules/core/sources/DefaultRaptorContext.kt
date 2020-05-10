package io.fluidsonic.raptor


internal class DefaultRaptorContext(
	override val properties: DefaultRaptorPropertySet
) : RaptorContext, RaptorScope {

	override val context: RaptorContext
		get() = this


	override val parent: RaptorContext?
		get() = null


	override fun asScope(): RaptorScope =
		this


	override fun toString() = buildString {
		append("[context] ->")

		if (context.properties.isEmpty()) {
			append(" (empty)")
			return@buildString
		}

		append("\n")
		append(context.properties.toString().prependIndent("\t"))
	}
}
