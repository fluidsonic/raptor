package io.fluidsonic.raptor.graphql.internal


internal class GraphDirective(
	val name: String,
) {

	companion object {

		val optional = GraphDirective(name = "optional")
	}
}
