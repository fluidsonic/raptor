package io.fluidsonic.raptor.domain


@JvmInline
public value class RaptorAggregateEventId(private val value: Long) : RaptorEntityId {

	override val discriminator: String
		get() = "aggregate event"


	public fun toLong(): Long =
		value


	override fun toString(): String =
		value.toString()
}
