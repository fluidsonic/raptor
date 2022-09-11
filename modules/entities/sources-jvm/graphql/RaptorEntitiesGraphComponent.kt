package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*
import kotlin.reflect.*


internal class RaptorEntitiesGraphComponent internal constructor(
	private val graphComponent: RaptorGraphComponent,
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
			graphComponent.definitions(RaptorEntityId.graphDefinition(idDefinitionsByDiscriminator.values))
	}


	internal object Key : RaptorComponentKey2<RaptorEntitiesGraphComponent> {

		override fun toString() = "entities"
	}
}


@RaptorDsl
public fun RaptorGraphComponent.definition(definition: RaptorEntityId.Definition<*>) {
	definitions(definition.graphDefinition())

	componentRegistry2.oneOrRegister(RaptorEntitiesGraphComponent.Key) {
		RaptorEntitiesGraphComponent(graphComponent = this)
	}.addIdDefinition(definition)
}
