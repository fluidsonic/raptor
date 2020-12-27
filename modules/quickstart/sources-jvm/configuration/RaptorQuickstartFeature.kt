package io.fluidsonic.raptor

import io.ktor.http.*


public object RaptorQuickstartFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorQuickstartFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		install(BsonRaptorFeature)
		install(RaptorDIFeature)
		install(RaptorEntitiesFeature)
		install(RaptorKtorFeature)

		bson {
			includeDefaultDefinitions()
			includeMongoClientDefaultCodecs()

			definitions(
				PasswordHash.bsonDefinition(),
				Url.bsonDefinition() // FIXME move to either BsonFeature or KtorFeature with ifAvailable
			)
		}

		graphs {
			includeDefaultDefinitions()

			definitions(
				AccessToken.graphDefinition(),
				Password.graphDefinition()
			)
		}

		di {
			provide { PasswordHasher() }
		}
	}
}


public const val raptorQuickstartFeatureId: RaptorFeatureId = "raptor.quickstart"
