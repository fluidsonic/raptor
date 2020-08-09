package io.fluidsonic.raptor

import io.ktor.http.*


object RaptorQuickstartFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorQuickstartFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		install(BsonRaptorFeature)
		install(RaptorDIFeature)
		install(KtorRaptorFeature)

		bson {
			includeDefaultCodecs()
			includeMongoClientDefaultCodecs()

			definitions(
				PasswordHash.bsonDefinition(),
				TypedId.bsonDefinition(),
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


const val raptorQuickstartFeatureId: RaptorFeatureId = "raptor.quickstart"
