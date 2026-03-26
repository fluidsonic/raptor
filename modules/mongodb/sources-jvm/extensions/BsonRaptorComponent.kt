package io.fluidsonic.raptor.mongo

import com.mongodb.*
import com.mongodb.client.gridfs.codecs.*
import com.mongodb.client.model.geojson.codecs.*
import com.mongodb.client.model.mql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.codecs.jsr310.*


private val mongoClientDefaultCodecsIncludedComponentExtensionKey = RaptorComponentExtensionKey<Boolean>("MongoClient default codecs included")

private val defaultMongoCodecRegistry = CodecRegistries.fromProviders(
	listOf(
		ValueCodecProvider(),
		BsonValueCodecProvider(),
		DBRefCodecProvider(),
		DBObjectCodecProvider(),
		DocumentCodecProvider(DocumentToDBRefTransformer()),
		CollectionCodecProvider(DocumentToDBRefTransformer()),
		IterableCodecProvider(DocumentToDBRefTransformer()),
		MapCodecProvider(DocumentToDBRefTransformer()),
		GeoJsonCodecProvider(),
		GridFSFileCodecProvider(),
		Jsr310CodecProvider(),
		JsonObjectCodecProvider(),
		BsonCodecProvider(),
//		EnumCodecProvider(), // Must be encoded explicitly.
		ExpressionCodecProvider(),
		Jep395RecordCodecProvider(),
//		KotlinCodecProvider(), // Must be encoded explicitly.
	)
)


@RaptorDsl
public fun RaptorBsonComponent.includeMongoClientDefaultCodecs() {
	if (extensions[mongoClientDefaultCodecsIncludedComponentExtensionKey] != true) {
		extensions[mongoClientDefaultCodecsIncludedComponentExtensionKey] = true

		providers(defaultMongoCodecRegistry, priority = RaptorBsonDefinition.Priority.low)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorBsonComponent>.includeMongoClientDefaultCodecs() {
	this {
		includeMongoClientDefaultCodecs()
	}
}
