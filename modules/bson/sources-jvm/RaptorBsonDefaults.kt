package io.fluidsonic.raptor

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import kotlin.time.*
import kotlinx.datetime.*
import org.bson.codecs.*
import org.bson.codecs.jsr310.*


public object RaptorBsonDefaults {

	public val definitions: List<RaptorBsonDefinition> = listOf(
		CollectionExtensions.bsonDefinition(),
		Country.bsonDefinition(),
		CountryCode.bsonDefinition(),
		Currency.bsonDefinition(),
		CurrencyCode.bsonDefinition(),
		DayOfWeek_bsonDefinition(),
		Duration.bsonDefinition(),
		GeoCoordinate.bsonDefinition(),
		LocalDate.bsonDefinition(),
		LocalDateTime.bsonDefinition(),
		LocalTime.bsonDefinition(),
		Timestamp.bsonDefinition(),
		TimeZone.bsonDefinition(),
		RaptorBsonDefinition.of(ValueCodecProvider()),
		RaptorBsonDefinition.of(BsonValueCodecProvider()),
		RaptorBsonDefinition.of(Jsr310CodecProvider()),
		RaptorBsonDefinition.of(BsonCodecProvider())
	)
}
