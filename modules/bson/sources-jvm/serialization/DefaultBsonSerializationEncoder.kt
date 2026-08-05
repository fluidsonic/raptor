package io.fluidsonic.raptor.bson

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*
import org.bson.*


@OptIn(ExperimentalSerializationApi::class)
internal class DefaultBsonSerializationEncoder(
	private val writer: BsonWriter,
	override val serializersModule: SerializersModule,
	private val preserveNulls: Boolean,
) : AbstractEncoder(), RaptorBsonSerializationEncoder {

	override val bsonWriter: BsonWriter
		get() = writer


	override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
		when (val kind = descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> writer.writeStartDocument()
			StructureKind.LIST -> writer.writeStartArray()
			else -> throw SerializationException(
				"Raptor's BSON serialization format cannot encode a structure of kind $kind (${descriptor.serialName})."
			)
		}

		return this
	}


	override fun encodeBoolean(value: Boolean) {
		writer.writeBoolean(value)
	}


	override fun encodeDouble(value: Double) {
		writer.writeDouble(value)
	}


	// BSON array elements carry positional names (`"0"`, `"1"`, …) that the writer generates itself, so only
	// document fields get an explicit name here.
	override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
		if (descriptor.kind != StructureKind.LIST)
			writer.writeName(descriptor.getElementName(index))

		return true
	}


	override fun encodeInt(value: Int) {
		writer.writeInt32(value)
	}


	override fun encodeLong(value: Long) {
		writer.writeInt64(value)
	}


	override fun encodeNull() {
		writer.writeNull()
	}


	// `kotlinx.serialization` routes every nullable property through this one method and `AbstractEncoder` leaves
	// it open, which is what makes omitting a field possible at all: returning here skips `encodeElement`, so not
	// even the field name reaches the writer. A null *array element* is never omitted — BSON array element names
	// are positions, so dropping one would silently shift every element after it.
	override fun <T : Any> encodeNullableSerializableElement(
		descriptor: SerialDescriptor,
		index: Int,
		serializer: SerializationStrategy<T>,
		value: T?,
	) {
		if (value === null && !preserveNulls && descriptor.kind != StructureKind.LIST)
			return

		super.encodeNullableSerializableElement(descriptor, index, serializer, value)
	}


	override fun encodeString(value: String) {
		writer.writeString(value)
	}


	override fun encodeValue(value: Any): Unit =
		throw SerializationException(
			"Raptor's BSON serialization format supports only `Int`, `Long`, `Double`, `Boolean` and `String` " +
				"as primitive values, not ${value::class}."
		)


	override fun endStructure(descriptor: SerialDescriptor) {
		when (descriptor.kind) {
			StructureKind.LIST -> writer.writeEndArray()
			else -> writer.writeEndDocument()
		}
	}
}
