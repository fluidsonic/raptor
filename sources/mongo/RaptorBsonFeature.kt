package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.http.*
import org.kodein.di.erased.*


object RaptorBsonFeature : RaptorFeature<RaptorBsonFeature.Builder> {

	val defaultDefinitions = listOf<RaptorBsonDefinition<*>>(
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
		TimeZone.bsonDefinition(),
		Url.bsonDefinition()
	)


	override fun RaptorConfigScope.configure(dslConfig: Builder.() -> Unit) {
		kodein {
			bind() from instance(BuilderImpl().apply(dslConfig).build())
		}
	}


	interface Builder {

		fun bsonDefinitions(vararg definitions: RaptorBsonDefinition<*>) = bsonDefinitions(definitions.asIterable())
		fun bsonDefinitions(definitions: Iterable<RaptorBsonDefinition<*>>)
	}


	private class BuilderImpl : Builder {

		private val definitions = defaultDefinitions.toMutableList()


		fun build() = Config(
			definitions = definitions
		)


		override fun bsonDefinitions(definitions: Iterable<RaptorBsonDefinition<*>>) {
			this.definitions += definitions
		}
	}


	private class Config(
		val definitions: List<RaptorBsonDefinition<*>>
	)
}
