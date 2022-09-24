import io.fluidsonic.raptor.cqrs.*


sealed interface BankAccountCommand : RaptorAggregateCommand<BankAccountNumber> {

	data class Create(val owner: String) : BankAccountCommand
	object Delete : BankAccountCommand
	data class Deposit(val amount: Int) : BankAccountCommand
	data class Label(val label: String) : BankAccountCommand
	data class Withdraw(val amount: Int) : BankAccountCommand
}
