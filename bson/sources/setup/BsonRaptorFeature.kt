package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.codecs.jsr310.*
import org.kodein.di.erased.*


object BsonRaptorFeature : RaptorFeature {

	private val defaultDefinitions = listOf<RaptorBsonDefinition<*>>(
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


	private val defaultProviders = listOf(
		ValueCodecProvider(),
		BsonValueCodecProvider(),
		Jsr310CodecProvider(),
		BsonCodecProvider()
	)


	override fun RaptorFeatureSetup.setup() {
		componentRegistry.register(BsonRaptorComponent())

		bson {
			definitions(defaultDefinitions)
			providers(defaultProviders)
		}
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val component = componentRegistry.getSingle<BsonRaptorComponent>()?.component
		val config = component?.complete() ?: BsonConfig.empty

		kodein {
			bind() from instance(config)

			bind<BsonScope>() with singleton {
				BsonScopeImpl(
					config = instance(),
					scope = instance()
				)
			}

			bind<CodecRegistry>() with singleton {
				instance<BsonScope>().codecRegistry
			}
		}
	}
}


// FIXME is it okay to automatically register the feature?
@Raptor.Dsl3
val RaptorFeatureComponent.bson: RaptorComponentConfig<BsonRaptorComponent>
	get() {
		install(BsonRaptorFeature)

		return componentRegistry.configureSingle()
	}
