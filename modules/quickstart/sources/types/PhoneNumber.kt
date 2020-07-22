package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ data class PhoneNumber(val value: String) {

	override fun toString() = value


	companion object {

		fun bsonDefinition() = bsonDefinition(
			parse = ::PhoneNumber,
			serialize = PhoneNumber::value
		)


		fun graphDefinition(): GraphScalarDefinition<PhoneNumber> = graphScalarDefinition {
			parseString(::PhoneNumber)

			parseJson(::PhoneNumber)
			serializeJson(PhoneNumber::value)
		}
	}
}
