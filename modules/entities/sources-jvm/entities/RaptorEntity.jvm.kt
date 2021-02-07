package io.fluidsonic.raptor


internal fun RaptorEntity.Companion.graphDefinition() = graphInterfaceDefinition(name = "Entity") {
	field(RaptorEntity::id)
}
