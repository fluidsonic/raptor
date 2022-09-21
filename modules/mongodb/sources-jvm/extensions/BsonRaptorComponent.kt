package io.fluidsonic.raptor.mongo

import com.mongodb.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*


@RaptorDsl
public fun RaptorBsonComponent.includeMongoClientDefaultCodecs() {
	if (extensions[MongoClientDefaultCodecsIncluded] != true) {
		extensions[MongoClientDefaultCodecsIncluded] = true

		providers(MongoClientSettings.getDefaultCodecRegistry(), priority = RaptorBsonDefinition.Priority.low)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.includeMongoClientDefaultCodecs() {
	this {
		includeMongoClientDefaultCodecs()
	}
}


private object MongoClientDefaultCodecsIncluded : RaptorComponentExtensionKey<Boolean> {

	override fun toString() = "MongoClient default codecs included"
}
