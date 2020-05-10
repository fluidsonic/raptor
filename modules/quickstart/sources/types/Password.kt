package io.fluidsonic.raptor


inline class Password(val value: String) {

	override fun toString() =
		"Password(***)"


	companion object {

		fun graphDefinition() = graphScalarDefinition {
			conversion {
				parseString(::Password)

				parseJson(::Password)
				serializeJson(Password::value)
			}
		}
	}
}
