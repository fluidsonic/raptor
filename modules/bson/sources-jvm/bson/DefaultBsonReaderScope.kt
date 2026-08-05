package io.fluidsonic.raptor.bson

import io.fluidsonic.time.*
import kotlin.reflect.*
import org.bson.*
import org.bson.types.*


internal class DefaultBsonReaderScope(
	parent: RaptorBsonScope,
	reader: BsonReader,
) : RaptorBsonReaderScope, RaptorBsonReader, RaptorBsonScope by parent, BsonReader by reader {

	private val underlyingReader = reader


	override fun boolean(): Boolean =
		underlyingReader.readBoolean()


	override fun bsonType(): BsonType =
		underlyingReader.currentBsonType


	override fun byteArray(): ByteArray =
		underlyingReader.readBinaryData().data


	// A codec that opted into [RaptorBsonTypeAwareCodec] gets the type itself — and with it the precomputed
	// per-argument types — instead of the raw type-argument list it would otherwise have to resolve
	// reflectively on every read.
	private fun codecValue(type: RaptorBsonType<*>): Any =
		when (val codec = type.codec(codecRegistry)) {
			is RaptorBsonTypeAwareCodec<*> -> codec.decode(scope = this, type = type)
			else -> codec.decode(scope = this, arguments = type.arguments)
		}


	private fun <Value, Destination : MutableCollection<Value>> collectionValue(type: KType, destination: Destination): Destination {
		val elementType = type.arguments.single().type ?: error("Cannot read elements of unknown type: $type")

		arrayByElement {
			destination.add(value(elementType))
		}

		return destination
	}


	private fun <Destination : MutableCollection<Any?>> collectionValue(
		type: RaptorBsonType<*>,
		destination: Destination,
	): Destination {
		val elementType = type.elementType ?: error("Cannot read elements of unknown type: ${type.type}")

		arrayByElement {
			destination.add(if (type.elementIsNullable) valueOrNull(elementType) else valueOrThrow(elementType))
		}

		return destination
	}


	override fun double(): Double =
		underlyingReader.readDouble()


	override fun endArray() {
		underlyingReader.readEndArray()
	}


	override fun endDocument() {
		underlyingReader.readEndDocument()
	}


	override fun fieldName(): String =
		underlyingReader.readName()


	override fun int(): Int =
		underlyingReader.readInt32()


	override fun internal(): BsonReader =
		underlyingReader


	override fun invalidValue(message: String): Nothing {
		error("BSON value is not valid: $message")
	}


	override fun long(): Long =
		when (underlyingReader.currentBsonType) {
			BsonType.INT32 -> underlyingReader.readInt32().toLong()
			else -> underlyingReader.readInt64()
		}


	override fun nextBsonType(): BsonType =
		underlyingReader.readBsonType()


	override fun missingFieldValue(name: String): Nothing {
		error("BSON object requires a value for field '$name'.")
	}


	override fun objectId(): ObjectId =
		underlyingReader.readObjectId()


	override val reader: RaptorBsonReader
		get() = this


	override fun skipValue() {
		underlyingReader.skipValue()
	}


	override fun startArray() {
		underlyingReader.readStartArray()
	}


	override fun startDocument() {
		underlyingReader.readStartDocument()
	}


	override fun string(): String =
		underlyingReader.readString()


	override fun timestamp(): Timestamp =
		Timestamp.fromEpochMilliseconds(underlyingReader.readDateTime())


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> valueOrThrow(type: RaptorBsonType<Value>): Value =
		when (bsonType()) {
			BsonType.NULL -> error("Cannot decode BSON null value as type '${type.type}'.")

			else -> when (type.collectionKind) {
				BsonCollectionKind.list -> collectionValue(type = type, destination = arrayListOf<Any?>()) as Value
				BsonCollectionKind.set -> collectionValue(type = type, destination = LinkedHashSet<Any?>()) as Value
				null -> codecValue(type) as Value
			}
		}


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> valueOrNull(type: RaptorBsonType<Value>): Value? =
		when (bsonType()) {
			BsonType.NULL -> {
				readNull()
				null
			}

			else -> valueOrThrow(type)
		}


	override fun valueOrThrow(type: RaptorBsonType<Boolean>): Boolean =
		boolean()


	override fun valueOrThrow(type: RaptorBsonType<Double>): Double =
		double()


	override fun valueOrThrow(type: RaptorBsonType<Int>): Int =
		int()


	override fun valueOrThrow(type: RaptorBsonType<Long>): Long =
		long()


	@Suppress("UNCHECKED_CAST")
	override fun <Value> value(type: KType): Value =
		when (bsonType()) {
			BsonType.NULL -> when (type.isMarkedNullable) {
				true -> {
					readNull()
					null as Value
				}

				false -> error("Cannot decode BSON null value as type '$type'.")
			}

			else -> {
				val classifier = type.classifier

				when (bsonCollectionKindOf(classifier)) {
					BsonCollectionKind.list ->
						collectionValue(type = type, destination = arrayListOf<Value>()) as Value

					BsonCollectionKind.set ->
						collectionValue(type = type, destination = LinkedHashSet<Value>()) as Value

					null -> {
						val valueClass = classifier as? KClass<*>
							?: error("Cannot decode type '$type'.")

						codecRegistry.decode(scope = this, valueClass = valueClass, arguments = type.arguments) as Value
					}
				}
			}
		}
}
