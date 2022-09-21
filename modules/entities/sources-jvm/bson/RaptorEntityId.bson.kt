package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.*
import org.bson.*
import org.bson.types.*


// FIXME different types
public fun <Id : RaptorEntityId> RaptorEntityId.Definition<Id>.bsonDefinition(): RaptorBsonDefinition =
	raptor.bson.definition(idDescriptor.instanceClass) {
		decode {
			idDescriptor.factory(when (reader.bsonType()) {
				BsonType.STRING -> reader.string()
				else -> reader.objectId().toString()
			})
		}

		encode { value ->
			val stringValue = value.toString()
			when (ObjectId.isValid(stringValue)) {
				true -> writer.value(ObjectId(stringValue))
				false -> writer.value(stringValue)
			}
		}
	}
