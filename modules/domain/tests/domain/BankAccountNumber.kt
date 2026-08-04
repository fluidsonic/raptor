import io.fluidsonic.raptor.domain.*


@JvmInline
value class BankAccountNumber(private val value: String) : RaptorAggregateProjectionId, FilterMarkerAggregateId {

	override val discriminator: String
		get() = "bank account"


	override fun toString() = value
}
