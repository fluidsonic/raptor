import io.fluidsonic.raptor.domain.*


@JvmInline
value class CarNumber(private val value: String) : RaptorAggregateProjectionId {

	override val discriminator: String
		get() = "car"


	override fun toString() = value
}
