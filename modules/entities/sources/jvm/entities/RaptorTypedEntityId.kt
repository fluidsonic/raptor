package io.fluidsonic.raptor


@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
public inline class RaptorTypedEntityId @PublishedApi internal constructor(
	private val untyped: RaptorEntityId,
) {

	override fun toString(): String =
		untyped.toString()


	public fun toUntyped(): RaptorEntityId =
		untyped


	public companion object
}


public fun RaptorEntityId.toTyped(): RaptorTypedEntityId =
	RaptorTypedEntityId(this)
