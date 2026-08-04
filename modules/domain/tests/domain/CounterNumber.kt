import io.fluidsonic.raptor.domain.*


@JvmInline
value class CounterNumber(private val value: String) : RaptorAggregateProjectionId, FilterMarkerAggregateId {

	override val discriminator: String
		get() = "counter"


	override fun toString() = value
}
