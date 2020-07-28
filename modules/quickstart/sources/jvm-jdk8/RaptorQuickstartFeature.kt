package io.fluidsonic.raptor

import io.ktor.http.*
import org.kodein.di.erased.*


object RaptorQuickstartFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorQuickstartFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		install(BsonRaptorFeature)
		install(RaptorKodeinFeature)
		install(KtorRaptorFeature)

		bson {
			includeDefaultCodecs()
			includeMongoClientDefaultCodecs()

			definitions(
				EmailAddress.bsonDefinition(),
				PasswordHash.bsonDefinition(),
				PhoneNumber.bsonDefinition(),
				TypedId.bsonDefinition(),
				Url.bsonDefinition() // FIXME move to either BsonFeature or KtorFeature with ifAvailable
			)
		}

		graphs {
			includeDefaultDefinitions()

			definitions(
				AccessToken.graphDefinition(),
				EmailAddress.graphDefinition(),
				Password.graphDefinition(),
				PhoneNumber.graphDefinition()
			)
		}

		kodein {
			bind() from singleton { PasswordHasher() }
		}
	}
}


const val raptorQuickstartFeatureId: RaptorFeatureId = "raptor.quickstart"
