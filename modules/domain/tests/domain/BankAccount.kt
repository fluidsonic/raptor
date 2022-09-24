import io.fluidsonic.raptor.cqrs.*


data class BankAccount(
	val amount: Int,
	override val id: BankAccountNumber,
	val label: String? = null,
	val owner: String,
) : RaptorProjection<BankAccountNumber>
