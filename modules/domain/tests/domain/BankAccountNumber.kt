import io.fluidsonic.raptor.cqrs.*


@JvmInline
value class BankAccountNumber(private val value: String) : RaptorAggregateId, RaptorProjectionId {

	override val discriminator: String
		get() = "bank account"


	override fun toString() = value
}
