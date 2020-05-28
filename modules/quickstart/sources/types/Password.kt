package io.fluidsonic.raptor


inline class Password(val value: String) {

	override fun toString() =
		"Password(***)"


	companion object {

		fun graphDefinition(): GraphScalarDefinition<Password> = graphScalarDefinition {
			parseString(::Password)

			parseJson(::Password)
			serializeJson(Password::value)
		}
	}
}
