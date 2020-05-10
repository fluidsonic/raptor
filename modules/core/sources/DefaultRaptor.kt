package io.fluidsonic.raptor


internal class DefaultRaptor(
	override val context: DefaultRaptorContext
) : Raptor {

	override fun toString() = buildString {
		append("[raptor] ->\n")
		append(context.toString().prependIndent("\t"))
	}
}
