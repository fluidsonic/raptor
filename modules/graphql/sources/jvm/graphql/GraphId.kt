package io.fluidsonic.raptor


/**
 * A type that resolves to `ID` in a GraphQL schema.
 */
public inline class GraphId(public val value: String) {

	override fun toString(): String =
		value


	public companion object {

		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition(name = "ID") {
			parse { input ->
				when (input) {
					is Int -> GraphId(input.toString())
					is String -> GraphId(input)
					else -> invalid()
				}
			}
			serialize(GraphId::value)
		}
	}
}
