package io.fluidsonic.raptor.mongo2

import com.mongodb.reactivestreams.client.MongoCollection as SourceCollection
import kotlinx.coroutines.reactive.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.conversions.*


internal data class DefaultMongoCollection<Value : Any>(
	override val coderRegistry: MongoCoderRegistry,
	private val database: DefaultMongoDatabase,
	private val name: String,
	override val valueType: MongoValueType<Value>,
) : MutableMongoCollection<Value> {

	@Suppress("UNCHECKED_CAST")
	private val source = run {
		database.source
			.getCollection(name, ValueCodec::class.java)
			.withCodecRegistry(
				CodecRegistries.fromRegistries(
					CodecRegistries.fromCodecs(
						ValueCodec(
							coderRegistry = coderRegistry,
							valueType = valueType,
						)
					),
					coderRegistry.asLegacy(),
				)
			)
			.let { it as SourceCollection<Value> }
	}


	override suspend fun deleteMany(filter: Bson, options: MongoDeleteOptions) =
		MongoDeleteResult.fromSource(source.deleteMany(filter, options.toSource()).awaitSingle())


	override suspend fun deleteOne(filter: Bson, options: MongoDeleteOptions) =
		MongoDeleteResult.fromSource(source.deleteOne(filter, options.toSource()).awaitSingle())


	override suspend fun drop() {
		source.drop().awaitEmpty()
	}


	override fun find(filter: Bson?) =
		DefaultMongoFindFlow(source = source.find()).filter(filter)


	override suspend fun insertOne(value: Value, options: MongoInsertOneOptions) =
		MongoInsertOneResult.fromSource(source.insertOne(value, options.toSource()).awaitSingle())


	override suspend fun insertMany(values: List<Value>, options: MongoInsertManyOptions) =
		MongoInsertManyResult.fromSource(source.insertMany(values, options.toSource()).awaitSingle())


	override suspend fun replaceOne(filter: Bson, replacement: Value, options: MongoReplaceOptions) =
		MongoUpdateResult.fromSource(source.replaceOne(filter, replacement, options.toSource()).awaitSingle())


	override suspend fun updateMany(filter: Bson, update: Bson, options: MongoUpdateOptions) =
		MongoUpdateResult.fromSource(source.updateMany(filter, update, options.toSource()).awaitSingle())


	override suspend fun updateOne(filter: Bson, update: Bson, options: MongoUpdateOptions) =
		MongoUpdateResult.fromSource(source.updateOne(filter, update, options.toSource()).awaitSingle())


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoCollection<Value> =
		when (coderRegistry) {
			this.coderRegistry -> this
			else -> copy(coderRegistry = coderRegistry)
		}


	@Suppress("UNCHECKED_CAST")
	override fun <NewValue : Any> withValueType(valueType: MongoValueType<NewValue>): MutableMongoCollection<NewValue> =
		when (valueType) {
			this.valueType -> this as MutableMongoCollection<NewValue>
			else -> DefaultMongoCollection(
				coderRegistry = coderRegistry,
				database = database,
				name = name,
				valueType = valueType,
			)
		}


	private class ValueCodec<Value : Any>(
		private val coderRegistry: MongoCoderRegistry,
		private val valueType: MongoValueType<Value>,
	) : Codec<Value> {

		private val decoder by lazy { coderRegistry.decoder.find(valueType) }
		private val encoder by lazy { coderRegistry.encoder.find(valueType) }


		override fun decode(reader: BsonReader, unused: DecoderContext): Value =
			with(decoder) {
				DefaultMongoDecoderContext(LegacyMongoBsonReader(reader), coderRegistry.decoder).decode(valueType)
			}


		override fun encode(writer: BsonWriter, value: Value, unused: EncoderContext) {
			with(encoder) {
				DefaultMongoEncoderContext(LegacyMongoBsonWriter(writer), coderRegistry.encoder).encode(value, valueType)
			}
		}


		@Suppress("UNCHECKED_CAST")
		override fun getEncoderClass(): Class<Value> =
			ValueCodec::class.java as Class<Value>
	}
}
