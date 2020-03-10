package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*


class BsonRaptorSetup internal constructor() {

	private val definitions = defaultDefinitions.toMutableList()


	internal fun complete() = BsonRaptorConfig(
		definitions = definitions
	)


	fun definitions(vararg definitions: RaptorBsonDefinition<*>) =
		definitions(definitions.asIterable())


	fun definitions(definitions: Iterable<RaptorBsonDefinition<*>>) {
		this.definitions += definitions
	}


	companion object {

		private val defaultDefinitions = listOf<RaptorBsonDefinition<*>>(
			Cents.bsonDefinition(),
			Country.bsonDefinition(),
			Currency.bsonDefinition(),
			DayOfWeek.bsonDefinition(),
			EmailAddress.bsonDefinition(),
			GeoCoordinate.bsonDefinition(),
			LocalDate.bsonDefinition(),
			LocalTime.bsonDefinition(),
			Money.bsonDefinition(),
			PasswordHash.bsonDefinition(),
			PreciseDuration.bsonDefinition(),
			PhoneNumber.bsonDefinition(),
			Timestamp.bsonDefinition(),
			TimeZone.bsonDefinition()
			//Url.bsonDefinition() // FIXME requires ktor
		)
	}
}
