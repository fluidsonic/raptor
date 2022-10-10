import io.fluidsonic.raptor.domain.*


data class BankAccount(
	val amount: Int,
	override val id: BankAccountNumber,
	val label: String? = null,
	val owner: String,
) : RaptorAggregateProjection<BankAccountNumber>
