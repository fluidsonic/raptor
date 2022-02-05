package io.fluidsonic.raptor


@JvmInline
public value class RaptorTypedEntityId internal constructor(
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
