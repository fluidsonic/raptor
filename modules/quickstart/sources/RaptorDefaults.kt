package io.fluidsonic.raptor

import io.ktor.http.*
import org.kodein.di.erased.*


@RaptorDsl
fun RaptorRootComponent.installDefaults() {
	install(BsonRaptorFeature)
	install(KtorRaptorFeature)

	bson {
		includeDefaultCodecs()
		includeMongoClientDefaultCodecs()

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
