package io.fluidsonic.raptor

import io.fluidsonic.raptor.quickstart.internal.*


inline class TypedId(val untyped: EntityId) {

	override fun toString() =
		untyped.toString()


	companion object
}


internal fun TypedId.Companion.bsonDefinition() = bsonDefinition<TypedId> {
	val factoryByType: Map<String, EntityId.Factory<*>> = findEntityIdDefinitions(context.bsonConfiguration.definitions)
		.map { it.factory }
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
			write("type", value.untyped.factory.type)
			write("id", value.untyped)
		}
	}
}


private fun findEntityIdDefinitions(definition: RaptorBsonDefinitions): Collection<EntityIdBsonDefinition<*>> =
	(definition as? EntityIdBsonDefinition<*>)?.let(::listOf)
		?: definition.underlyingDefinitions.flatMap(::findEntityIdDefinitions)
