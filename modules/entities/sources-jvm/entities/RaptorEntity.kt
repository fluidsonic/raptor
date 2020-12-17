package io.fluidsonic.raptor


public interface RaptorEntity {

	public val id: RaptorEntityId


	public companion object {

		internal fun graphDefinition() = graphInterfaceDefinition(name = "Entity") {
			field(RaptorEntity::id)
		}
	}
}
