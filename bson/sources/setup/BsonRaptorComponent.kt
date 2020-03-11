package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*


@Raptor.Dsl3
class BsonRaptorComponent internal constructor() : RaptorComponent {

	internal val definitions = defaultDefinitions.toMutableList()

	override val raptorSetupContext: RaptorSetupContext
		get() = TODO()


	internal fun complete() = BsonRaptorConfig(
		definitions = definitions
	)


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


@Raptor.Dsl3
fun RaptorConfigurable<BsonRaptorComponent>.definitions(vararg definitions: RaptorBsonDefinition<*>) {
	definitions(definitions.asIterable())
}


@Raptor.Dsl3
fun RaptorConfigurable<BsonRaptorComponent>.definitions(definitions: Iterable<RaptorBsonDefinition<*>>) {
	forEachComponent {
		this.definitions += definitions
	}
}
