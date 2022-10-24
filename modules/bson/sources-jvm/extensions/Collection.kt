package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*


internal object CollectionExtensions {

	// TODO Support decoding of common subtypes.
	internal fun bsonDefinition() = raptor.bson.definition<Collection<*>> {
		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type ?: error("Explicit type argument required in order to decode a Collection.")

			val elements = mutableListOf<Any?>()

			reader.arrayByElement {
				elements.add(value(valueType))
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
