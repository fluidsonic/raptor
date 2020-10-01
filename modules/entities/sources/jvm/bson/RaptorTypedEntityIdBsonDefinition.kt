package io.fluidsonic.raptor

import kotlin.reflect.full.*


internal fun RaptorTypedEntityId.Companion.bsonDefinition(
	definitions: Collection<RaptorEntityIdDefinition<*>>,
) = raptor.bson.definition<RaptorTypedEntityId> {
	val discriminatorByClass = definitions.associate { it.type to it.discriminator }
	val typesByDiscriminator = definitions.associate { it.discriminator to it.type.starProjectedType }

	decode {
		reader.document {
			val type = string("typeId").let { typeId ->
				typesByDiscriminator[typeId] ?: error("No entity definition with discriminator '$typeId' provided.")
			}

			fieldName("id")
			value<RaptorEntityId>(type).toTyped()
		}
	}

	encode { value ->
		writer.document {
			val id = value.toUntyped()
			val typeId = discriminatorByClass[id::class] ?: error("No entity definition with ID type '${id::class.qualifiedName}' provided.")

			value("typeId", typeId)
			value("id", id)
		}
	}
}
