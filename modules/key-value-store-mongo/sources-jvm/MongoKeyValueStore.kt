package io.fluidsonic.raptor.keyvaluestore.mongo

import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.keyvaluestore.RaptorKeyValueStore.*
import io.fluidsonic.raptor.mongo.*
import kotlin.reflect.*
import kotlinx.coroutines.flow.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


private class MongoKeyValueStore<Key : Any, Value : Any>(
	private val collection: MongoCollection<Entry<Key, Value>>,
	private val keyClass: KClass<Key>,
	private val valueClass: KClass<Value>,
) : RaptorKeyValueStore<Key, Value> {

	override suspend fun clear() {
		collection.deleteMany(Document())
	}


	override fun entries(): Flow<Entry<Key, Value>> =
		collection.find()


	override fun keys(): Flow<Key> =
		collection.distinct(KeyValueEntryBsonCodec.Fields.key, keyClass)


	override fun values(): Flow<Value> =
		collection.distinct(KeyValueEntryBsonCodec.Fields.value, valueClass)


	override suspend fun set(key: Key, value: Value) {
		collection.replaceOneById(key, Entry(key, value), ReplaceOptions().upsert(true))
	}


	override suspend fun remove(key: Key) {
		collection.deleteOneById(key)
	}


	override suspend fun get(key: Key): Value? =
		collection.findOneById(key)?.value
}


@Suppress("FunctionName")
internal fun <Key : Any, Value : Any> MongoKeyValueStore(
	database: MongoDatabase,
	collectionName: String,
	keyClass: KClass<Key>,
	valueClass: KClass<Value>,
): RaptorKeyValueStore<Key, Value> =
	MongoKeyValueStore(
		collection = database.getCollectionOf<Entry<Key, Value>>(collectionName)
			.withCodecRegistry(CodecRegistries.fromRegistries(
				CodecRegistries.fromCodecs(KeyValueEntryBsonCodec(
					keyCodec = database.codecRegistry.get(keyClass.java),
					valueCodec = database.codecRegistry.get(valueClass.java),
				)),
				database.codecRegistry,
			)),
		keyClass = keyClass,
		valueClass = valueClass,
	)


@Suppress("FunctionName")
internal inline fun <reified Key : Any, reified Value : Any> MongoKeyValueStore(
	database: MongoDatabase,
	collectionName: String,
): RaptorKeyValueStore<Key, Value> =
	MongoKeyValueStore(
		database = database,
		collectionName = collectionName,
		keyClass = Key::class,
		valueClass = Value::class,
	)


private class KeyValueEntryBsonCodec<Key : Any, Value : Any>(
	private val keyCodec: Codec<Key>,
	private val valueCodec: Codec<Value>,
) : Codec<Entry<Key, Value>> {

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Entry<Key, Value> {
		reader.readName(Fields.key)
		val key = keyCodec.decode(reader, null)

		reader.readName(Fields.value)
		val value = valueCodec.decode(reader, null)

		return Entry(key = key, value = value)
	}


	override fun encode(writer: BsonWriter, value: Entry<Key, Value>, encoderContext: EncoderContext) {
		writer.writeName(Fields.key)
		keyCodec.encode(writer, value.key, null)

		writer.writeName(Fields.value)
		valueCodec.encode(writer, value.value, null)
	}


	@Suppress("UNCHECKED_CAST")
	override fun getEncoderClass(): Class<Entry<Key, Value>> =
		Entry::class.java as Class<Entry<Key, Value>>


	object Fields {

		const val key = "_id"
		const val value = "value"
	}
}
