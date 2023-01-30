package io.fluidsonic.raptor.domain


@JvmInline
public value class RaptorAggregateEventId(private val value: Long) : RaptorEntityId, Comparable<RaptorAggregateEventId> {

	override val discriminator: String
		get() = "aggregate event"


	override fun compareTo(other: RaptorAggregateEventId): Int =
		value.compareTo(other.value)


	public fun toLong(): Long =
		value


	override fun toString(): String =
		value.toString()
}
