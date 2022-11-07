import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.domain.*


class BankAccountAggregate(
	override val id: BankAccountNumber,
) : RaptorAggregate<BankAccountNumber, BankAccountCommand, BankAccountChange> {

	private var amount = 0
	private var isCreated = false
	private var label: String? = null


	override fun copy() =
		BankAccountAggregate(id).also { copy ->
			copy.amount = amount
			copy.isCreated = isCreated
			copy.label = label
		}


	override fun execute(command: BankAccountCommand): List<BankAccountChange> =
		listOfNotNull(when (command) {
			is Create -> execute(command)
			is Delete -> execute(command)
			is Deposit -> execute(command)
			is Label -> execute(command)
			is Withdraw -> execute(command)
		})


	private fun execute(command: Create): Created {
		check(!isCreated) { "Already created." }

		return Created(owner = command.owner)
	}


	private fun execute(@Suppress("UNUSED_PARAMETER") command: Delete): Deleted? {
		check(amount == 0) { "Cannot delete an account that still has funds." }

		if (!isCreated)
			return null

		return Deleted
	}


	private fun execute(command: Deposit): Deposited? {
		check(isCreated) { "Not yet created." }

		if (command.amount == 0)
			return null

		return Deposited(amount = command.amount)
	}


	private fun execute(command: Label): Labeled? {
		check(isCreated) { "Not yet created." }

		if (command.label == label)
			return null

		return Labeled(label = command.label)
	}


	private fun execute(command: Withdraw): Withdrawn? {
		check(isCreated) { "Not yet created." }
		check(command.amount <= amount) { "Not enough funds (${command.amount} required, $amount available)." }

		if (command.amount == 0)
			return null

		return Withdrawn(amount = command.amount)
	}


	override fun handle(change: BankAccountChange) {
		when (change) {
			is Created -> handle(change)
			is Deleted -> handle(change)
			is Deposited -> handle(change)
			is Labeled -> handle(change)
			is Withdrawn -> handle(change)
		}
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Created) {
		isCreated = true
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Deleted) {
		isCreated = false
	}


	private fun handle(event: Deposited) {
		amount += event.amount
	}


	private fun handle(event: Labeled) {
		label = event.label
	}


	private fun handle(event: Withdrawn) {
		amount -= event.amount
	}


	override fun toString() =
		"BankAccountAggregate(id=$id, amount=$amount, label=$label)"
}
