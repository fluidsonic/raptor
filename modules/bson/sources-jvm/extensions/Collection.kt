package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import java.util.concurrent.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


internal object CollectionExtensions {

	private val bsonTypeClassMap = BsonTypeClassMap()
	private val decoderContext = DecoderContext.builder().build()

	// `BsonTypeCodecMap`'s constructor resolves a codec for every `BsonType`, which is expensive to redo on
	// every untyped-collection decode. Cache it per underlying registry instead of rebuilding it each time -
	// there is normally only one registry per process, but tests (and potentially other callers) can
	// construct several, so the cache must be keyed by registry identity rather than a single shared value.
	private val codecMapsByRegistry = ConcurrentHashMap<CodecRegistry, BsonTypeCodecMap>()


	// TODO Support decoding of common subtypes.
	internal fun bsonDefinition() = raptor.bson.definition<Collection<*>> {
		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type
			val elements = mutableListOf<Any?>()

			if (valueType == null) {
				val internalRegistry = codecRegistry.internal()
				val codecMap = codecMapsByRegistry.getOrPut(internalRegistry) { BsonTypeCodecMap(bsonTypeClassMap, internalRegistry) }

				reader.arrayByElement {
					val codec = codecMap.get(bsonType())
					elements += codec.decode(reader.internal(), decoderContext)
				}
			}
			else {
				reader.arrayByElement {
					elements += value<Any?>(valueType)
				}
			}

			elements
		}

		encode(includingSubclasses = true) { value ->
			writer.array {
				for (element in value)
					value(element)
			}
		}
	}
}
