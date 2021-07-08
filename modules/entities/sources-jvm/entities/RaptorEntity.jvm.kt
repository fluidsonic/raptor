package io.fluidsonic.raptor


// FIXME check
internal fun RaptorEntity.Companion.graphDefinition() = graphInterfaceDefinition<RaptorEntity>(name = "Entity") {
	field(RaptorEntity::id)
}
