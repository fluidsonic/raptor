package io.fluidsonic.raptor.mongo2

import com.mongodb.*
import com.mongodb.reactivestreams.client.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


internal class DefaultMongoClientBuilder : MutableMongoClient.Builder {

	private var coderRegistry: MongoCoderRegistry? = null
	private var connectionString: String? = null


	override fun build(): MutableMongoClient =
		DefaultMongoClient(
			coderRegistry = MongoCoderRegistry(
				decoder = MongoDecoderRegistry(IntCoder, SetCoder, StringCoder),
				encoder = MongoEncoderRegistry(IntCoder, SetCoder, StringCoder)
			).let { def ->
				when (coderRegistry) {
					null -> def
					else -> coderRegistry!! + def
				}
			},// FIXME
			source = MongoClients.create(
				MongoClientSettings.builder()
					.applyConnectionString(ConnectionString(connectionString ?: error("Must provide `connectionString()`.")))
					.codecRegistry(CodecRegistries.fromCodecs(BsonDocumentCodec())) // FIXME needed?
					.build(),
			),
		)


	override fun coderRegistry(coderRegistry: MongoCoderRegistry) = also {
		this.coderRegistry = coderRegistry
	}


	override fun connectionString(connectionString: String) = also {
		this.connectionString = connectionString
	}
}
