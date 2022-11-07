package io.fluidsonic.raptor.domain


public interface RaptorEntityId {

	public val discriminator: String
		get() = this::class.qualifiedName ?: "<unknown>"


	override fun toString(): String
}


public val RaptorEntityId.debug: String
	get() = "$this ($discriminator)"
