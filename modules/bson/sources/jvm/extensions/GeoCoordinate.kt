package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.stdlib.GeoCoordinate.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<GeoCoordinate> {
	decode {
		var coordinate: GeoCoordinate? = null
		var type: String? = null

		reader.documentByField { fieldName ->
			when (fieldName) {
				"coordinates" -> coordinate = array {
					val longitude = double()
					val latitude = double()

					GeoCoordinate(latitude = latitude, longitude = longitude)
				}
				"type" -> type = string()
				else -> skipValue()
			}
		}

		check(type == "Point") { "Invalid GeoCoordinate type: $type" }

		coordinate ?: error("Missing coordinate.")
	}

	encode { value ->
		writer.document {
			array("coordinates") {
				value(value.longitude)
				value(value.latitude)
			}
			this.value("type", "Point")
		}
	}
}
