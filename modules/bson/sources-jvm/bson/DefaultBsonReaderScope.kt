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


	private fun <Value, Destination : MutableCollection<Value>> collectionValue(type: KType, destination: Destination): Destination {
		val elementType = type.arguments.single().type ?: error("Cannot read elements of unknown type: $type")

		arrayByElement {
			destination.add(value(elementType))
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
	override fun <Value> value(type: KType): Value =
		when (bsonType()) {
			BsonType.NULL -> when (type.isMarkedNullable) {
				true -> {
					readNull()
					null as Value
				}

				false -> error("Cannot decode BSON null value as type '$type'.")
			}

			else -> when (type.classifier) {
				ArrayList::class, MutableList::class, List::class, MutableCollection::class, Collection::class ->
					collectionValue(type = type, destination = arrayListOf<Value>()) as Value

				HashSet::class, LinkedHashSet::class, MutableSet::class, Set::class ->
					collectionValue(type = type, destination = LinkedHashSet<Value>()) as Value

				else -> {
					val valueClass = type.classifier as? KClass<*>
						?: error("Cannot decode type '$type'.")

					codecRegistry.decode(scope = this, valueClass = valueClass, arguments = type.arguments) as Value
				}
			}
		}
}
