package io.fluidsonic.raptor

import kotlin.reflect.full.*


internal fun RaptorTypedEntityId.Companion.bsonDefinition(
	definitions: Collection<RaptorEntityId.Definition<*>>,
) = raptor.bson.definition<RaptorTypedEntityId> {
	val discriminatorsByClass = definitions.associate { it.idDescriptor.instanceClass to it.idDescriptor.discriminator }
	val typesByDiscriminator = definitions.associate {
		it.idDescriptor.discriminator to it.idDescriptor.instanceClass.starProjectedType
	}

	decode {
		reader.document {
			val type = string("type").let { discriminator ->
				typesByDiscriminator[discriminator] ?: error("No definition provided for entity IDs with discriminator '$discriminator'.")
			}

			fieldName("id")
			value<RaptorEntityId>(type).toTyped()
		}
	}

	encode { value ->
		writer.document {
			val id = value.toUntyped()
			val discriminator = discriminatorsByClass[id::class]
				?: error("No definition provided for entity IDs with type '${id::class.qualifiedName}'.")

			value("type", discriminator)
			value("id", id)
		}
	}
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.definition(
	definition: RaptorEntityId.Definition<*>,
	priority: RaptorBsonDefinition.Priority = RaptorBsonDefinition.Priority.normal,
) {
	definitions(definition.bsonDefinition(), priority = priority)

	configure {
		componentRegistry.oneOrRegister(RaptorEntitiesBsonComponent.Key) {
			RaptorEntitiesBsonComponent(bsonComponent = this)
		}.addIdDefinition(definition)
	}
}
