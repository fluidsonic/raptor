@file:JvmName("BsonRaptorComponent+MongoDB")

package io.fluidsonic.raptor

import com.mongodb.*


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.includeMongoClientDefaultCodecs() = configure {
	if (extensions[MongoClientDefaultCodecsIncluded] != true) {
		extensions[MongoClientDefaultCodecsIncluded] = true

		registries(MongoClientSettings.getDefaultCodecRegistry())
	}
}


private object MongoClientDefaultCodecsIncluded : RaptorComponentExtensionKey<Boolean> {

	override fun toString() = "MongoClient default codecs included"
}
