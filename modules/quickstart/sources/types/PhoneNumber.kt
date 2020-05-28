package io.fluidsonic.raptor


inline class PhoneNumber(val value: String) {

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
