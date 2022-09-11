package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*


// FIXME check
internal fun RaptorEntity.Companion.graphDefinition() = graphInterfaceDefinition<RaptorEntity>(name = "Entity") {
	field(RaptorEntity::id)
}
