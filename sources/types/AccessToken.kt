package io.fluidsonic.raptor


inline class AccessToken(val value: String) {

	override fun toString() = value


	companion object {

		fun graphDefinition() = graphScalarDefinition {
			conversion {
				parseString(::AccessToken)

				parseJson(::AccessToken)
				serializeJson(AccessToken::value)
			}
		}
	}
}
