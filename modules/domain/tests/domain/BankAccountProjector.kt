import BankAccountChange.*
import io.fluidsonic.raptor.domain.*


internal class BankAccountProjector : RaptorAggregateProjector.Incremental<BankAccount, BankAccountNumber, BankAccountChange> {

	override var projection: BankAccount? = null
		private set


	override fun add(event: RaptorAggregateEvent<BankAccountNumber, BankAccountChange>) =
		projection.apply(event).also { projection = it }


	private fun BankAccount?.apply(event: RaptorAggregateEvent<BankAccountNumber, BankAccountChange>): BankAccount? {
		if (this != null)
			check(id == event.aggregateId) { "Cannot apply event for aggregate ${event.aggregateId} to $id." }

		@Suppress("KotlinConstantConditions")
		return when (val change = event.change) {
			is Created -> when (this) {
				null -> BankAccount(
					amount = 0,
					id = event.aggregateId,
					owner = change.owner,
				)

				else -> error("Cannot create aggregate $id multiple times.")
			}

			else -> when (this) {
				null -> error("Missing first event.")
				else -> when (change) {
					is Created -> error("Compiler error.s")
					is Deleted -> null
					is Deposited -> apply(change)
					is Labeled -> apply(change)
					is Withdrawn -> apply(change)
				}
			}
		}
	}


	private fun BankAccount.apply(event: Deposited): BankAccount =
		copy(amount = amount + event.amount)


	private fun BankAccount.apply(event: Labeled): BankAccount =
		copy(label = event.label)


	private fun BankAccount.apply(event: Withdrawn): BankAccount =
		copy(amount = amount - event.amount)
}
