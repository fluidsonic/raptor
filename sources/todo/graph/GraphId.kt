package io.fluidsonic.raptor


inline class GraphId(val value: String) {

	override fun toString() =
		value


	companion object {

		fun graphDefinition() = graphScalarDefinition {
			name("Id")

			conversion {
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
}
