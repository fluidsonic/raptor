package io.fluidsonic.raptor.keyvaluestore.mongo

import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.*
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


internal class MongoKeyValueStore<Key : Any, Value : Any>(
	collection: MongoCollection<Document>,
	private val keyClass: KClass<Key>,
	private val valueClass: KClass<Value>,
) : RaptorKeyValueStore<Key, Value> {

	@Suppress("UNCHECKED_CAST")
	private val collection: MongoCollection<Entry<Key, Value>> = collection
		.withCodecRegistry(CodecRegistries.fromRegistries(
			CodecRegistries.fromCodecs(EntryCodec(
				keyCodec = collection.codecRegistry.get(keyClass.java),
				valueCodec = collection.codecRegistry.get(valueClass.java),
			)),
			collection.codecRegistry,
		))
		.withDocumentClass(Entry::class) as MongoCollection<Entry<Key, Value>>


	override suspend fun clear() {
		collection.deleteMany(Document())
	}


	override fun entries(): Flow<Pair<Key, Value>> =
		collection.find().map { it.toPair() }


	override fun keys(): Flow<Key> =
		collection.findOneField(Fields.key, keyClass)


	override fun values(): Flow<Value> =
		collection.findOneField(Fields.value, valueClass)


	override suspend fun set(key: Key, value: Value) {
		collection.replaceOneById(key, Entry(key, value), ReplaceOptions().upsert(true))
	}


	override suspend fun setIfAbsent(key: Key, value: Value): Boolean =
		collection.updateOne(
			filter = eq(Fields.key, key),
			update = setOnInsert(Fields.value, value),
			options = UpdateOptions().upsert(true),
		).upsertedId != null


	override suspend fun remove(key: Key): Boolean =
		collection.deleteOneById(key).deletedCount > 0


	override suspend fun get(key: Key): Value? =
		collection.findOneById(key)?.value


	private data class Entry<out Key : Any, out Value : Any>(
		val key: Key,
		val value: Value,
	) {

		fun toPair(): Pair<Key, Value> =
			key to value
	}


	private class EntryCodec<Key : Any, Value : Any>(
		private val keyCodec: Codec<Key>,
		private val valueCodec: Codec<Value>,
	) : Codec<Entry<Key, Value>> {

		override fun decode(reader: BsonReader, decoderContext: DecoderContext): Entry<Key, Value> {
			reader.readStartDocument()

			reader.readName(Fields.key)
			val key = keyCodec.decode(reader, decoderContext)

			reader.readName(Fields.value)
			val value = valueCodec.decode(reader, decoderContext)

			reader.readEndDocument()

			return Entry(key = key, value = value)
		}


		override fun encode(writer: BsonWriter, value: Entry<Key, Value>, encoderContext: EncoderContext) {
			writer.writeStartDocument()

			writer.writeName(Fields.key)
			keyCodec.encode(writer, value.key, encoderContext)

			writer.writeName(Fields.value)
			valueCodec.encode(writer, value.value, encoderContext)

			writer.writeEndDocument()
		}


		@Suppress("UNCHECKED_CAST")
		override fun getEncoderClass(): Class<Entry<Key, Value>> =
			Entry::class.java as Class<Entry<Key, Value>>
	}


	private object Fields {

		const val key = "_id"
		const val value = "value"
	}
}


@Suppress("FunctionName")
internal inline fun <reified Key : Any, reified Value : Any> MongoKeyValueStore(
	collection: MongoCollection<Document>,
): RaptorKeyValueStore<Key, Value> =
	MongoKeyValueStore(collection = collection, keyClass = Key::class, valueClass = Value::class)
