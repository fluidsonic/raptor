package io.fluidsonic.raptor

import kotlin.reflect.*


public class RaptorEntitiesComponent internal constructor() : RaptorComponent.Default<RaptorEntitiesComponent>() {

	private var idDefinitionsByType: Map<KClass<out RaptorEntityId>, RaptorEntityIdDefinition<*>> = emptyMap()


	internal fun addIdDefinition(definition: RaptorEntityIdDefinition<*>) {
		check(!idDefinitionsByType.containsKey(definition.type)) {
			"Cannot provide multiple entity ID definitions with the same type '${definition.type.qualifiedName}'."
		}

		idDefinitionsByType = idDefinitionsByType + (definition.type to definition)
	}


	internal fun complete() =
		idDefinitionsByType.values


	public companion object;


	internal object Key : RaptorComponentKey<RaptorEntitiesComponent> {

		override fun toString() = "entities"
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorEntitiesComponent>.definitions(
	vararg definitions: RaptorEntityIdDefinition<*>,
) {
	definitions(definitions.asIterable())
}


@RaptorDsl
public fun RaptorComponentSet<RaptorEntitiesComponent>.definitions(
	definitions: Iterable<RaptorEntityIdDefinition<*>>,
) {
	configure {
		definitions.forEach(::addIdDefinition)
	}
}
