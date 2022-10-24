package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*


@Suppress("RemoveExplicitTypeArguments")
internal fun RaptorEntity.Companion.graphDefinition() = graphInterfaceDefinition<RaptorEntity>(name = "Entity") {
	field(RaptorEntity::id)
}
