package io.fluidsonic.raptor


inline class AccessToken(val value: String) {

	override fun toString() = value


	companion object {

		fun graphDefinition(): GraphScalarDefinition<AccessToken> = graphScalarDefinition {
			parseString(::AccessToken)

			parseJson(::AccessToken)
			serializeJson(AccessToken::value)
		}
	}
}
