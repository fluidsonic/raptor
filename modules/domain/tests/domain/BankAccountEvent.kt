import io.fluidsonic.raptor.cqrs.*


sealed interface BankAccountEvent : RaptorAggregateEvent<BankAccountNumber> {

	data class Created(val owner: String) : BankAccountEvent
	object Deleted : BankAccountEvent
	data class Deposited(val amount: Int) : BankAccountEvent
	data class Labeled(val label: String) : BankAccountEvent
	data class Withdrawn(val amount: Int) : BankAccountEvent
}
