package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.mongo.*
import io.ktor.http.*


public object RaptorQuickstartPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		install(RaptorBsonPlugin)
		install(RaptorDIPlugin)
		install(RaptorEntitiesPlugin)
		install(RaptorKtorPlugin)

		bson {
			includeDefaultDefinitions()
			includeMongoClientDefaultCodecs()

			definitions(
				PasswordHash.bsonDefinition(),
				Url.bsonDefinition() // TODO move to either BsonPlugin or KtorPlugin with ifAvailable
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
			provide<PasswordHasher> { PasswordHasher() }
		}
	}
}
