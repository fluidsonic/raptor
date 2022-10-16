package io.fluidsonic.raptor.domain


@JvmInline
public value class RaptorAggregateEventId(private val value: String) : RaptorEntityId {

	override val discriminator: String
		get() = "aggregate event"


	override fun toString(): String =
		value
}