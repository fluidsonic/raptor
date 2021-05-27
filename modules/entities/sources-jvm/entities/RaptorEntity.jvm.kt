package io.fluidsonic.raptor


// FIXME check
internal fun RaptorEntity.Companion.graphDefinition() = graphInterfaceDefinition(name = "Entity") {
	field(RaptorEntity::id)
}
