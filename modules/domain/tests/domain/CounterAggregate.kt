import CounterChange.*
import CounterCommand.*
import io.fluidsonic.raptor.domain.*


class CounterAggregate(
	override val id: CounterNumber,
) : RaptorAggregate<CounterNumber, CounterCommand, CounterChange> {

	private var isCreated = false
	private var value = 0


	override fun copy() =
		CounterAggregate(id).also { copy ->
			copy.isCreated = isCreated
			copy.value = value
		}


	override fun execute(command: CounterCommand): List<CounterChange> =
		listOfNotNull(when (command) {
			is Create -> execute(command)
			is Increment -> execute(command)
		})


	private fun execute(@Suppress("UNUSED_PARAMETER") command: Create): Created? {
		if (isCreated)
			return null

		return Created
	}


	private fun execute(@Suppress("UNUSED_PARAMETER") command: Increment): Incremented {
		check(isCreated) { "Not yet created." }

		return Incremented
	}


	override fun handle(change: CounterChange) {
		when (change) {
			is Created -> handle(change)
			is Incremented -> handle(change)
		}
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Created) {
		isCreated = true
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Incremented) {
		value += 1
	}


	override fun toString() =
		"CounterAggregate(id=$id, value=$value)"
}
