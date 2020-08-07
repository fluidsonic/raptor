package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import org.bson.codecs.*
import org.bson.codecs.jsr310.*


public object RaptorBsonDefaults {

	public val definitions: RaptorBsonDefinitions = RaptorBsonDefinitions.of(
		RaptorBsonDefinitions.of(
			Cents.bsonDefinition(),
			Country.bsonDefinition(),
			CountryCode.bsonDefinition(),
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
		),
		RaptorBsonDefinitions.of(
			ValueCodecProvider(),
			BsonValueCodecProvider(),
			Jsr310CodecProvider(),
			BsonCodecProvider()
		)
	)
}
