package io.fluidsonic.raptor


public abstract class RaptorKey<Value : Any>(
	public val label: String,
) {

	final override fun equals(other: Any?): Boolean =
		this === other


	final override fun hashCode(): Int =
		super.hashCode()


	final override fun toString(): String =
		label
}
