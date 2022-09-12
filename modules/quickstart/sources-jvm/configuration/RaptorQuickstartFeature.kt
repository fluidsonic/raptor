package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.http.*


public object RaptorQuickstartFeature : RaptorFeature {

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

		graphs.all.definitions {
			includeDefault()

			add(
				AccessToken.graphDefinition(),
				Password.graphDefinition()
			)
		}

		di {
			provide { PasswordHasher() }
		}
	}
}
