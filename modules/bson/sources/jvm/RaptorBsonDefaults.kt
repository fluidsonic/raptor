package io.fluidsonic.raptor

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.codecs.jsr310.*


public object RaptorBsonDefaults {

	public val definitions: RaptorBsonDefinitions = Definitions


	private object Definitions : RaptorBsonDefinitions {

		private val root = RaptorBsonDefinitions.of(
			RaptorBsonDefinitions.of(
				Country.bsonDefinition(),
				CountryCode.bsonDefinition(),
				Currency.bsonDefinition(),
				DayOfWeek.bsonDefinition(),
				GeoCoordinate.bsonDefinition(),
				LocalDate.bsonDefinition(),
				LocalDateTime.bsonDefinition(),
				LocalTime.bsonDefinition(),
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


		override val underlyingDefinitions = listOf(root)


		override fun createCodecRegistry(scope: BsonScope): CodecRegistry =
			root.createCodecRegistry(scope)


		override fun toString() = "<BSON definition: Raptor defaults>"
	}
}
