package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun GeoCoordinate.Companion.bsonDefinition() = bsonDefinition<GeoCoordinate> {
	decode {
		var coordinate: GeoCoordinate? = null
		var type: String? = null

		readDocumentWithValues { fieldName ->
			when (fieldName) {
				"coordinates" -> coordinate = readArray {
					val longitude = readDouble()
					val latitude = readDouble()

					GeoCoordinate(latitude = latitude, longitude = longitude)
				}
				"type" -> type = readString()
				else -> skipValue()
			}
		}

		if (type != "Point")
			throw BsonException("invalid type for GeoCoordinate: $type")

		coordinate ?: throw BsonException("missing coordinate")
	}


	encode { value ->
		writeDocument {
			writeArray("coordinates") {
				writeDouble(value.longitude)
				writeDouble(value.latitude)
			}
			writeString("type", "Point")
		}
	}
}
