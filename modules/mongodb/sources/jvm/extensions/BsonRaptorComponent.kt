@file:JvmName("BsonRaptorComponent+MongoDB")

package io.fluidsonic.raptor

import com.mongodb.*


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.includeMongoClientDefaultCodecs(): Unit = configure {
	if (extensions[MongoClientDefaultCodecsIncluded] != true) {
		extensions[MongoClientDefaultCodecsIncluded] = true

		registries(MongoClientSettings.getDefaultCodecRegistry(), priority = RaptorBsonDefinitions.Priority.low)
	}
}


private object MongoClientDefaultCodecsIncluded : RaptorComponentExtensionKey<Boolean> {

	override fun toString() = "MongoClient default codecs included"
}
