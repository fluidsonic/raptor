package io.fluidsonic.raptor

import kotlin.reflect.full.*


public fun <Id : RaptorEntityId> RaptorEntityId.Definition<Id>.graphDefinition(): RaptorGraphDefinition =
	graphIdAliasDefinition<Id>(idDescriptor.instanceClass.starProjectedType) {
		parse { decodeOrNull(it) ?: invalid("\"$it\" is not a valid '${idDescriptor.discriminator}' ID.") }
		serialize { encode(it, includeDiscriminator = true) }
	}


internal fun RaptorEntityId.Companion.graphDefinition(definitions: Collection<RaptorEntityId.Definition<*>>): RaptorGraphDefinition =
	graphIdAliasDefinition<RaptorEntityId> {
		val discriminatorsByClass = definitions.associate { it.idDescriptor.instanceClass to it.idDescriptor.discriminator }
		val definitionsByDiscriminator = definitions.associateBy { it.idDescriptor.discriminator }

		parse { string ->
			val definition = string.substringBefore(":", missingDelimiterValue = "")
				.ifEmpty { invalidValueError() }
				.let { discriminator ->
					definitionsByDiscriminator[discriminator] ?: error("No definition provided for entity IDs with discriminator '$discriminator'.")
				}

			definition.decodeOrNull(string, requireDiscriminator = true)
				?: invalid("\"$string\" is not a valid '${definition.idDescriptor.discriminator}' ID.")
		}
		serialize { id ->
			val discriminator = discriminatorsByClass[id::class]
				?: error("No definition provided for entity IDs with type '${id::class.qualifiedName}'.")

			"$discriminator:$id"
		}
	}
