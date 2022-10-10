import io.fluidsonic.raptor.domain.*


sealed interface BankAccountChange : RaptorAggregateChange<BankAccountNumber> {

	data class Created(val owner: String) : BankAccountChange
	object Deleted : BankAccountChange
	data class Deposited(val amount: Int) : BankAccountChange
	data class Labeled(val label: String) : BankAccountChange
	data class Withdrawn(val amount: Int) : BankAccountChange
}
