package io.fluidsonic.raptor


/**
 * A type that resolves to `ID` in a GraphQL schema.
 */
public inline class GraphId(public val value: String) {

	override fun toString(): String =
		value


	public companion object {

		public fun graphDefinition(): GraphScalarDefinition<GraphId> = graphScalarDefinition(name = "ID") {
			parseInt { GraphId(it.toString()) }
			parseString(::GraphId)

			parseJson<Any> { value ->
				when (value) {
					is Int -> GraphId(value.toString())
					is String -> GraphId(value)
					else -> invalid()
				}
			}
			serializeJson(GraphId::value)
		}
	}
}
