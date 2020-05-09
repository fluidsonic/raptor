package io.fluidsonic.raptor

import io.ktor.http.*
import org.kodein.di.erased.*


@Raptor.Dsl3
fun RaptorCoreComponent.installDefaults() {
	install(BsonRaptorFeature)
	install(KtorRaptorFeature)
	install(MongoRaptorFeature)

	bson {
		definitions(
			EmailAddress.bsonDefinition(),
			PasswordHash.bsonDefinition(),
			PhoneNumber.bsonDefinition(),
			TypedId.bsonDefinition(),
			Url.bsonDefinition()
		)
	}

	graphs {
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
