package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ data class Password(val value: String) {

	override fun toString() =
		"Password(***)"


	companion object {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
			parseString(::Password)
			serialize(Password::value)
		}
	}
}
