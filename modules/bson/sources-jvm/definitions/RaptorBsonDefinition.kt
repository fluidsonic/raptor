package io.fluidsonic.raptor.bson

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import kotlin.time.*
import kotlinx.datetime.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.codecs.jsr310.*


public interface RaptorBsonDefinition {

	public fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Value>?


	public companion object {

		public val bsonDefaults: List<RaptorBsonDefinition> = listOf(
			of(ValueCodecProvider()),
			of(BsonValueCodecProvider()),
			of(Jsr310CodecProvider()),
			of(BsonCodecProvider())
		)

		public val raptorDefaults: List<RaptorBsonDefinition> = listOf(
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
		)


		@RaptorInternalApi
		public fun of(codec: Codec<*>): RaptorBsonDefinition =
			BsonCodecDefinition(codec)


		@RaptorInternalApi
		public fun of(provider: CodecProvider): RaptorBsonDefinition =
			BsonCodecProviderDefinition(provider)
	}


	public enum class Priority {

		high,
		normal,
		low
	}


	public interface ForValue<Value : Any> : RaptorBsonDefinition {

		public val valueClass: KClass<Value>
	}
}
