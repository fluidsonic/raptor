package io.fluidsonic.raptor


inline class GraphId(val value: String) {

	override fun toString() =
		value


	companion object {

		fun graphDefinition(): GraphScalarDefinition<GraphId> = graphScalarDefinition {
			name("ID")

			parseInt { GraphId(it.toString()) }
			parseString(::GraphId)

			parseJson<Any> { value ->
				when (value) {
					is Int -> GraphId(value.toString())
					is String -> GraphId(value)
					else -> null
				}
			}
			serializeJson(GraphId::value)
		}
	}
}
