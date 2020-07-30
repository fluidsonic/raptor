package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ data class EmailAddress(val value: String) {

	fun toLowerCase() =
		EmailAddress(value.toLowerCase())


	override fun toString() = value


	companion object {

		fun bsonDefinition() = bsonDefinition(
			parse = ::EmailAddress,
			serialize = EmailAddress::value
		)


		fun graphDefinition(): GraphScalarDefinition<EmailAddress> = graphScalarDefinition {
			parseString(::EmailAddress)

			parseJson(::EmailAddress)
			serializeJson(EmailAddress::value)
		}
	}
}
