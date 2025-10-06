package io.fluidsonic.raptor.keyvaluestore.mongo2

import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.mongo2.*
import io.fluidsonic.raptor.mongo2.MongoFilters.eq
import io.fluidsonic.raptor.mongo2.MongoUpdates.setOnInsert
import kotlinx.coroutines.flow.*


internal class MongoKeyValueStore<Key : Any, Value : Any>(
	database: MutableMongoDatabase,
	collectionName: String,
	private val keyType: MongoValueType<Key>,
	private val valueType: MongoValueType<Value>,
) : RaptorKeyValueStore<Key, Value> {

	private val collection: MutableMongoCollection<KeyValue<Key, Value>> =
		database
			.collection(
				name = collectionName,
				valueType = MongoValueType<KeyValue<Key, Value>>(keyType, valueType),
			)
			.withCoderRegistry(KeyValueCoder(keyType, valueType) + database.coderRegistry)


	override suspend fun clear() {
		collection.drop()
	}


	override fun entries(): Flow<Pair<Key, Value>> =
		collection.find().map { it.toPair() }


	override suspend fun get(key: Key): Value? =
		collection.findOneById(key)?.value


	// FIXME
	override fun keys(): Flow<Key> =
		collection.findFieldValues(Fields.key, true, keyType) // FIXME


	override suspend fun set(key: Key, value: Value) {
		collection.replaceOneById(key, KeyValue(key, value), MongoReplaceOptions(upsert = true))
	}


	override suspend fun setIfAbsent(key: Key, value: Value): Boolean =
		collection.updateOne(
			filter = eq(Fields.key, key),
			update = setOnInsert(Fields.value, value),
			options = MongoUpdateOptions(upsert = true),
		)
			.let { it ?: error("MongoDB key-value store requires a write concern with acknowledgements enabled.") }
			.upsertedId != null


	override suspend fun remove(key: Key): Boolean =
		collection.deleteOneById(key)
			.let { it ?: error("MongoDB key-value store requires a write concern with acknowledgements enabled.") }
			.deletedCount > 0


	// FIXME
	override fun values(): Flow<Value> =
		collection.findFieldValues(Fields.value, fieldValueIsNullable = true, valueType) // FIXME


	private data class KeyValue<out Key : Any?, out Value : Any?>(
		val key: Key,
		val value: Value,
	) {

		fun toPair(): Pair<Key, Value> =
			key to value
	}


	// FIXME
	private class KeyValueCoder<Key, Value>(
		private val keyType: MongoValueType<Key & Any>, // FIXME nullability
		private val valueType: MongoValueType<Value & Any>,
	) : MongoCoder<KeyValue<Key, Value>> {

		override fun decodes(type: MongoValueType<in KeyValue<Key, Value>>) =
			type.classifier == KeyValue::class


		override fun encodes(type: MongoValueType<out KeyValue<Key, Value>>) =
			type.classifier == KeyValue::class


		override fun MongoDecoderScope.decode(type: MongoValueType<in KeyValue<Key, Value>>): KeyValue<Key, Value> =
			document {
				fieldName(Fields.key)
				val key = with(context.decoderRegistry.find(keyType)) {
					decode(keyType)
				}

				fieldName(Fields.value)
				val value = with(context.decoderRegistry.find(valueType)) {
					decode(valueType)
				}

				KeyValue(key = key, value = value)
			}


		override fun MongoEncoderScope.encode(value: KeyValue<Key, Value>, type: MongoValueType<out KeyValue<Key, Value>>) {
			document {
				fieldName(Fields.key)
				with(context.encoderRegistry.find(keyType)) {
					when (val key = value.key) {
						null -> nullValue()
						else -> encode(key, keyType)
					}
				}

				fieldName(Fields.value)
				with(context.encoderRegistry.find(valueType)) {
					when (val value = value.value) {
						null -> nullValue()
						else -> encode(value, valueType)
					}
				}
			}
		}
	}


	private object Fields {

		const val key = "_id"
		const val value = "value"
	}
}
