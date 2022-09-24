package io.fluidsonic.raptor.mongo

import com.mongodb.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*

private val mongoClientDefaultCodecsIncludedComponentExtensionKey = RaptorComponentExtensionKey<Boolean>("MongoClient default codecs included")


@RaptorDsl
public fun RaptorBsonComponent.includeMongoClientDefaultCodecs() {
	if (extensions[mongoClientDefaultCodecsIncludedComponentExtensionKey] != true) {
		extensions[mongoClientDefaultCodecsIncludedComponentExtensionKey] = true

		providers(MongoClientSettings.getDefaultCodecRegistry(), priority = RaptorBsonDefinition.Priority.low)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorBsonComponent>.includeMongoClientDefaultCodecs() {
	this {
		includeMongoClientDefaultCodecs()
	}
}
