package io.fluidsonic.raptor

import io.ktor.http.*


@Raptor.Dsl3
fun RaptorComponentScope<RaptorCoreFeatureComponent>.installDefaults() {
	install(BsonRaptorFeature)
	install(KtorRaptorFeature)
	install(MongoRaptorFeature)

	bson {
		definitions(
			EmailAddress.bsonDefinition(),
			PasswordHash.bsonDefinition(),
			PhoneNumber.bsonDefinition(),
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
}
