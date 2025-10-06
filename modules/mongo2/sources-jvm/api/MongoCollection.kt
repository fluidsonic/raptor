package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import kotlin.reflect.*
import org.bson.*
import org.bson.conversions.*


public interface MongoCollection<out Value : Any> {

	public val coderRegistry: MongoCoderRegistry
	public val valueType: MongoValueType<out Value>

	public fun find(filter: Bson? = null): MongoFindFlow<Value>
	public fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoCollection<Value>
	public fun <NewValue : Any> withValueType(valueType: MongoValueType<NewValue>): MongoCollection<NewValue>


	public fun asImmutable(): MongoCollection<Value> =
		ImmutableMongoCollection(this)


	public fun <FieldValue> findFieldValues(
		fieldName: String,
		fieldValueIsNullable: Boolean,
		fieldValueType: MongoValueType<FieldValue & Any>,
	): MongoFindFlow<FieldValue> =
		withCoderRegistry(
			MongoCollectionFindFieldValuesValueDecoder(
				fieldName = fieldName,
				fieldValueIsNullable = fieldValueIsNullable,
				fieldValueType = fieldValueType,
			) + coderRegistry
		)
			.withValueType(MongoValueType<MongoCollectionFindFieldValuesValue<FieldValue>>(fieldValueType))
			.find()
			.projection(
				if (fieldName == "_id") Projections.include(fieldName)
				else Projections.fields(Projections.include(fieldName), Projections.excludeId())
			)
			.map { it.value }


	public suspend fun findOneById(id: Any?): Value? =// FIXME typed?
		find(Filters.eq(id)).firstOrNull()


	public companion object
}

public inline fun <reified Value> MongoCollection<*>.findFieldValues(fieldName: String): MongoFindFlow<Value> =
	findFieldValues(
		fieldName = fieldName,
		fieldValueIsNullable = typeOf<Value>().isMarkedNullable,
		fieldValueType = MongoValueType<Value>(),
	)


public inline fun <reified NewValue : Any> MongoCollection<*>.withValueType(): MongoCollection<NewValue> =
	withValueType(MongoValueType<NewValue>())


private data class MongoCollectionFindFieldValuesValue<out Value>(
	val value: Value,
)

// FIXME refactor
private class MongoCollectionFindFieldValuesValueDecoder<out Value>(
	private val fieldName: String,
	private val fieldValueIsNullable: Boolean,
	private val fieldValueType: MongoValueType<Value & Any>,
) : MongoDecoder<MongoCollectionFindFieldValuesValue<Value>> {

	// FIXME can't be safe
	override fun decodes(type: MongoValueType<in MongoCollectionFindFieldValuesValue<Value>>): Boolean =
		type.classifier == MongoCollectionFindFieldValuesValue::class


	// FIXME can't be safe if we control decodes() above
	@Suppress("UNCHECKED_CAST")
	override fun MongoDecoderScope.decode(
		type: MongoValueType<in MongoCollectionFindFieldValuesValue<Value>>,
	): MongoCollectionFindFieldValuesValue<Value> =
		MongoCollectionFindFieldValuesValue(document {
			when (val bsonType = nextBsonType()) {
				BsonType.END_OF_DOCUMENT -> {
					check(fieldValueIsNullable) {
						"Field '$fieldName' doesn't exist in all documents but type '$fieldValueType' is not nullable."
					}

					null
				}

				else -> {
					fieldName().let { actualName ->
						check(actualName == fieldName) {
							"Expected exactly one field '$fieldName' but field '$actualName' was returned."
						}
					}

					val value = when (bsonType) {
						BsonType.NULL -> {
							check(fieldValueIsNullable) {
								"Field '$fieldName' is 'null' in some documents but type '$fieldValueType' is not nullable."
							}
							nullValue()

							null
						}

						else ->
							with(context.decoderRegistry.find(fieldValueType)) {
								decode(fieldValueType)
							}
					}

					check(nextBsonType() == BsonType.END_OF_DOCUMENT) {
						"Expected exactly one field '$fieldName' but multiple were returned."
					}

					value
				}
			}
		} as Value)
}
