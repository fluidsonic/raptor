package io.fluidsonic.raptor.store.mongo

import com.mongodb.*
import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.RaptorKeyValueStore.*
import io.fluidsonic.raptor.mongo.*
import kotlin.reflect.*
import kotlinx.coroutines.flow.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


private val bsonDecoderContext = DecoderContext.builder().build()!!


internal class MongoKeyValueStore<Key : Any, Value : Any>(
	collection: MongoCollection<Document>,
	private val keyClass: KClass<Key>,
	private val valueClass: KClass<Value>,
) : RaptorKeyValueStore<Key, Value> {

	private val entryCodec = EntryCodec(
		keyCodec = collection.codecRegistry.get(keyClass.java),
		valueCodec = collection.codecRegistry.get(valueClass.java),
	)

	@Suppress("UNCHECKED_CAST")
	private val collection: MongoCollection<Entry<Key, Value>> = collection
		.withCodecRegistry(CodecRegistries.fromRegistries(
			CodecRegistries.fromCodecs(entryCodec),
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


	override suspend fun update(
		key: Key,
		maxAttempts: Int,
		decide: (current: Value?) -> UpdateDecision<Value>,
	): Value? {
		// Raw view over the collection: the unaltered stored bytes act as the optimistic
		// compare-and-swap filter, so a write only applies if the document is still unchanged.
		val rawCollection = collection.withDocumentClass(RawBsonDocument::class)

		repeat(maxAttempts) {
			val currentRaw = rawCollection.findOneById(key)
			val current = currentRaw?.let { entryCodec.decode(it.asBsonReader(), bsonDecoderContext).value }

			when (val decision = decide(current)) {
				UpdateDecision.Keep ->
					return current

				UpdateDecision.Remove -> {
					if (currentRaw == null)
						return null

					if (rawCollection.deleteOne(filter = currentRaw).deletedCount > 0)
						return null
				}

				is UpdateDecision.Update -> {
					val replacement = RawBsonDocument(Entry(key, decision.value), entryCodec)

					val applied = when (currentRaw) {
						null -> rawCollection.insertIfAbsent(replacement)
						else -> rawCollection.replaceOne(filter = currentRaw, replacement = replacement).matchedCount > 0
					}
					if (applied)
						return decision.value
				}
			}

			// The decision lost the optimistic race because the document changed concurrently → retry.
		}

		throw RaptorOptimisticUpdateException(maxAttempts)
	}


	/**
	 * Inserts [document], returning `true` if it was stored or `false` if a concurrent insert
	 * already claimed the key — in which case the optimistic [update] loop re-reads and retries.
	 */
	private suspend fun MongoCollection<RawBsonDocument>.insertIfAbsent(document: RawBsonDocument): Boolean =
		try {
			insertOne(document)

			true
		}
		catch (e: MongoWriteException) {
			// A duplicate-key error means another writer won the race; anything else is a real failure.
			when (e.error.category) {
				ErrorCategory.DUPLICATE_KEY -> false
				else -> throw e
			}
		}


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
