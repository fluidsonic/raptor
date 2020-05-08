package io.fluidsonic.raptor


inline class EmailAddress(val value: String) {

	fun toLowerCase() =
		EmailAddress(value.toLowerCase())


	override fun toString() = value


	companion object {

		fun bsonDefinition() = bsonDefinition(
			parse = ::EmailAddress,
			serialize = EmailAddress::value
		)


		fun graphDefinition() = graphScalarDefinition {
			conversion {
				parseString(::EmailAddress)

				parseJson(::EmailAddress)
				serializeJson(EmailAddress::value)
			}
		}
	}
}
