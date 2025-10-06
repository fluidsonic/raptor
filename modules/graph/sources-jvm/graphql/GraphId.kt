package io.fluidsonic.raptor.graph


/**
 * A type that resolves to `ID` in a GraphQL schema.
 */
@JvmInline
public value class GraphId(public val value: String) {

	override fun toString(): String =
		value


	public companion object {

		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<GraphId>(name = "ID") {
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
