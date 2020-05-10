package io.fluidsonic.raptor

import kotlin.reflect.full.*


inline class TypedId(val untyped: EntityId) {

	override fun toString() =
		untyped.toString()


	companion object
}


internal fun TypedId.Companion.bsonDefinition() = bsonDefinition<TypedId> {
	val factoryByType: Map<String, EntityId.Factory<*>> = context.bsonConfiguration
		.definitions
		.mapNotNull { it.valueClass.companionObjectInstance as? EntityId.Factory<*> } // FIXME evil hack!
		.associateBy { it.type }

	decode {
		readDocument {
			val factory = readString("type").let { type ->
				factoryByType[type] ?: throw BsonException("ID type '$type' has not been registered with Raptor")
			}

			readName("id")
			readValueOfType(factory.idClass).typed
		}
	}

	encode { value ->
		writeDocument {
			write("type", string = value.untyped.factory.type)
			write("id", value = value.untyped)
		}
	}
}
