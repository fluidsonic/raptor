package io.fluidsonic.raptor.graph


internal class GraphDirective(
	val name: String,
) {

	companion object {

		val optional = GraphDirective(name = "optional")
	}
}
