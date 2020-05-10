package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import org.bson.codecs.*
import org.bson.codecs.jsr310.*


object RaptorBsonDefaults {

	val definitions = listOf<RaptorBsonDefinition<*>>(
		Cents.bsonDefinition(),
		Country.bsonDefinition(),
		Currency.bsonDefinition(),
		DayOfWeek.bsonDefinition(),
		GeoCoordinate.bsonDefinition(),
		LocalDate.bsonDefinition(),
		LocalDateTime.bsonDefinition(),
		LocalTime.bsonDefinition(),
		Money.bsonDefinition(),
		PreciseDuration.bsonDefinition(),
		Timestamp.bsonDefinition(),
		TimeZone.bsonDefinition()
	)


	val providers = listOf(
		ValueCodecProvider(),
		BsonValueCodecProvider(),
		Jsr310CodecProvider(),
		BsonCodecProvider()
	)
}
