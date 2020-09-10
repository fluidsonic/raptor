@file:JvmName("BsonRaptorComponent+MongoDB")

package io.fluidsonic.raptor

import com.mongodb.*


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.includeMongoClientDefaultCodecs(): Unit = configure {
	if (extensions[MongoClientDefaultCodecsIncluded] != true) {
		extensions[MongoClientDefaultCodecsIncluded] = true

		providers(MongoClientSettings.getDefaultCodecRegistry(), priority = RaptorBsonDefinition.Priority.low)
	}
}


private object MongoClientDefaultCodecsIncluded : RaptorComponentExtensionKey<Boolean> {

	override fun toString() = "MongoClient default codecs included"
}
