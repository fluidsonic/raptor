package tests

import io.fluidsonic.raptor.*


data class Node(
	val name: String,
	val children: List<Node> = emptyList()
) {

	override fun toString() =
		toString(indent = "")


	private fun toString(indent: String): String = buildString {
		append(indent)
		append("node(")
		append(name.prependIndent("$indent\t").trimStart())
		append(")")

		if (children.isNotEmpty()) {
			append(" ->\n")
			children.forEachIndexed { index, child ->
				if (index > 0)
					append("\n")

				append(child.toString(indent = "$indent\t"))
			}
		}
	}


	companion object {

		val rootPropertyKey = RaptorPropertyKey<Node>("root node")
	}
}
