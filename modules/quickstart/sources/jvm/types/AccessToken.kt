package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ data class AccessToken(val value: String) {

	override fun toString() = value


	companion object {

		fun graphDefinition(): GraphScalarDefinition<AccessToken> = graphScalarDefinition {
			parseString(::AccessToken)

			parseJson(::AccessToken)
			serializeJson(AccessToken::value)
		}
	}
}
