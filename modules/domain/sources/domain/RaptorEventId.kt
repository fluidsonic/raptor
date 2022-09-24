package io.fluidsonic.raptor.cqrs


@JvmInline
public value class RaptorEventId(private val value: String) : RaptorEntityId {

	override val discriminator: String
		get() = "event"


	override fun toString(): String =
		value
}
