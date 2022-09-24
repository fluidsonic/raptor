package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*
import kotlin.reflect.*


internal class RaptorEntitiesGraphComponent internal constructor(
	private val graphComponent: RaptorGraphComponent,
) : RaptorComponent.Base<RaptorEntitiesGraphComponent>() {

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


	override fun RaptorComponentConfigurationEndScope<RaptorEntitiesGraphComponent>.onConfigurationEnded() {
		if (idDefinitionsByDiscriminator.isNotEmpty())
			graphComponent.definitions.add(RaptorEntityId.graphDefinition(idDefinitionsByDiscriminator.values))
	}


	internal companion object {

		val key = RaptorComponentKey<RaptorEntitiesGraphComponent>("entities")
	}
}


@RaptorDsl
public fun RaptorGraphComponent.definition(definition: RaptorEntityId.Definition<*>) {
	definitions.add(definition.graphDefinition())

	componentRegistry.oneOrRegister(RaptorEntitiesGraphComponent.key) {
		RaptorEntitiesGraphComponent(graphComponent = this)
	}.addIdDefinition(definition)
}
