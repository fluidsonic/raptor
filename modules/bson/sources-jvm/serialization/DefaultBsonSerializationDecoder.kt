package io.fluidsonic.raptor.bson

import java.util.concurrent.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*
import org.bson.*


// The field-name → declaration-index mapping of a `SerialDescriptor` is fully determined by that descriptor
// and never changes, so it is computed once per descriptor instance and shared by every decoder — nothing
// per-property is allocated while decoding.
//
// `org.mongodb:bson-kotlinx` instead allocates an `AbstractBsonDecoder.ElementMetadata[]` plus one
// `ElementMetadata` per property for every structure it decodes (verified with `javap` on
// `bson-kotlinx:5.6.4`: `beginStructure` creates a fresh decoder per structure and its `initElementMetadata`
// fills that array), which is the allocation gap this format exists to avoid.
//
// Note that `PluginGeneratedSerialDescriptor.getElementIndex` is itself already backed by a cached map, so
// this map is not what closes the gap — it merely keeps the format independent of that implementation detail
// and works the same for hand-written descriptors.
private val fieldIndexesByDescriptor: ConcurrentHashMap<SerialDescriptor, Map<String, Int>> =
	ConcurrentHashMap()


private fun fieldIndexesFor(descriptor: SerialDescriptor): Map<String, Int> {
	fieldIndexesByDescriptor[descriptor]?.let { return it }

	val elementCount = descriptor.elementsCount
	val fieldIndexes = HashMap<String, Int>(elementCount)

	for (index in 0 until elementCount)
		fieldIndexes[descriptor.getElementName(index)] = index

	return fieldIndexesByDescriptor.putIfAbsent(descriptor, fieldIndexes) ?: fieldIndexes
}


@OptIn(ExperimentalSerializationApi::class)
internal class DefaultBsonSerializationDecoder(
	private val reader: BsonReader,
	override val serializersModule: SerializersModule,
) : AbstractDecoder(), RaptorBsonSerializationDecoder {

	override val bsonReader: BsonReader
		get() = reader


	// BSON arrays do not carry their length, so `decodeSequentially` cannot be supported and elements have to
	// be counted while reading. Only the innermost array's counter is hot; the rare enclosing ones are parked
	// in [enclosingArrayElementIndexes], which stays unallocated for the common non-nested case.
	private var arrayElementIndex = 0
	private var arrayNestingDepth = 0
	private var enclosingArrayElementIndexes: IntArray? = null


	override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
		when (val kind = descriptor.kind) {
			StructureKind.CLASS, StructureKind.OBJECT -> reader.readStartDocument()
			StructureKind.LIST -> {
				pushArrayElementIndex()
				reader.readStartArray()
			}
			else -> throw SerializationException(
				"Raptor's BSON serialization format cannot decode a structure of kind $kind (${descriptor.serialName})."
			)
		}

		return this
	}


	override fun decodeBoolean(): Boolean =
		reader.readBoolean()


	override fun decodeDouble(): Double =
		reader.readDouble()


	override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
		if (descriptor.kind == StructureKind.LIST) {
			if (reader.readBsonType() == BsonType.END_OF_DOCUMENT)
				return CompositeDecoder.DECODE_DONE

			return arrayElementIndex++
		}

		val fieldIndexes = fieldIndexesFor(descriptor)

		while (true) {
			if (reader.readBsonType() == BsonType.END_OF_DOCUMENT)
				return CompositeDecoder.DECODE_DONE

			val index = fieldIndexes[reader.readName()]
			if (index != null)
				return index

			reader.skipValue()
		}
	}


	override fun decodeInt(): Int =
		reader.readInt32()


	override fun decodeLong(): Long =
		reader.readInt64()


	// The reader is positioned on the value at this point — its field name has already been consumed by
	// `decodeElementIndex` — so the BSON type is known without moving the reader forward. Consuming the null
	// itself is `decodeNull`'s job, which `kotlinx.serialization` only calls once this returned `false`.
	override fun decodeNotNullMark(): Boolean =
		reader.currentBsonType != BsonType.NULL


	override fun decodeNull(): Nothing? {
		reader.readNull()

		return null
	}


	override fun decodeString(): String =
		reader.readString()


	override fun decodeValue(): Any =
		throw SerializationException(
			"Raptor's BSON serialization format supports only `Int`, `Long`, `Double`, `Boolean` and `String` " +
				"as primitive values."
		)


	override fun endStructure(descriptor: SerialDescriptor) {
		when (descriptor.kind) {
			StructureKind.LIST -> {
				reader.readEndArray()
				popArrayElementIndex()
			}
			else -> reader.readEndDocument()
		}
	}


	private fun popArrayElementIndex() {
		arrayNestingDepth -= 1

		if (arrayNestingDepth > 0) {
			val enclosingIndexes = checkNotNull(enclosingArrayElementIndexes) {
				"An enclosing BSON array element index must have been recorded at nesting depth $arrayNestingDepth."
			}

			arrayElementIndex = enclosingIndexes[arrayNestingDepth - 1]
		}
	}


	private fun pushArrayElementIndex() {
		if (arrayNestingDepth > 0) {
			var enclosingIndexes = enclosingArrayElementIndexes

			if (enclosingIndexes === null || enclosingIndexes.size < arrayNestingDepth) {
				enclosingIndexes = enclosingIndexes?.copyOf(arrayNestingDepth * 2) ?: IntArray(4)
				enclosingArrayElementIndexes = enclosingIndexes
			}

			enclosingIndexes[arrayNestingDepth - 1] = arrayElementIndex
		}

		arrayNestingDepth += 1
		arrayElementIndex = 0
	}
}
