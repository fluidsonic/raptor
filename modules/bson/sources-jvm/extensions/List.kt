package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import org.bson.codecs.*


internal object ListExtensions {

	private val bsonTypeClassMap = BsonTypeClassMap()
	private val decoderContext = DecoderContext.builder().build()


	// TODO Support decoding of common subtypes.
	internal fun bsonDefinition() = raptor.bson.definition<List<*>> {
		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type
			val elements = mutableListOf<Any?>()

			if (valueType == null) {
				// TODO Hack. Rework the BSON definition system.
				val codecMap = BsonTypeCodecMap(bsonTypeClassMap, codecRegistry.internal())

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
