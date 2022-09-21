package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*


internal class RaptorEntitiesBsonComponent internal constructor(
	private val bsonComponent: RaptorBsonComponent,
) : RaptorComponent2.Base() {

	private var idDefinitionsByDiscriminator: MutableMap<String, RaptorEntityId.Definition<*>> = hashMapOf()
	private var idDefinitionsByInstanceClass: MutableMap<KClass<out RaptorEntityId>, RaptorEntityId.Definition<*>> = hashMapOf()


	internal fun addIdDefinition(definition: RaptorEntityId.Definition<*>) {
		val discriminator = definition.idDescriptor.discriminator
		idDefinitionsByDiscriminator[discriminator]?.let { existingDefinition ->
			check(definition === existingDefinition) {
				"Cannot add multiple ID definitions with the same discriminator '$discriminator'.\n" +
					"First: $existingDefinition\n" +
					"Second: $definition"
			}

			return
		}

		val instanceClass = definition.idDescriptor.instanceClass
		idDefinitionsByInstanceClass[instanceClass]?.let { existingDefinition ->
			check(definition === existingDefinition) {
				"Cannot add multiple ID definitions for the same class '${instanceClass.qualifiedName}'.\n" +
					"First: $existingDefinition\n" +
					"Second: $definition"
			}

			return
		}

		idDefinitionsByDiscriminator[discriminator] = definition
		idDefinitionsByInstanceClass[instanceClass] = definition
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		if (idDefinitionsByDiscriminator.isNotEmpty())
			bsonComponent.definitions(RaptorTypedEntityId.bsonDefinition(idDefinitionsByDiscriminator.values))
	}


	internal object Key : RaptorComponentKey2<RaptorEntitiesBsonComponent> {

		override fun toString() = "entities"
	}
}


@RaptorDsl
public fun RaptorBsonComponent.definition(
	definition: RaptorEntityId.Definition<*>,
	priority: RaptorBsonDefinition.Priority = RaptorBsonDefinition.Priority.normal,
) {
	definitions(definition.bsonDefinition(), priority = priority)

	componentRegistry2.oneOrRegister(RaptorEntitiesBsonComponent.Key) {
		RaptorEntitiesBsonComponent(bsonComponent = this)
	}.addIdDefinition(definition)
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.definition(
	definition: RaptorEntityId.Definition<*>,
	priority: RaptorBsonDefinition.Priority = RaptorBsonDefinition.Priority.normal,
) {
	this {
		definition(definition, priority = priority)
	}
}
