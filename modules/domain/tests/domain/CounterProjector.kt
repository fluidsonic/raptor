import CounterChange.*
import io.fluidsonic.raptor.domain.*


internal class CounterProjector : RaptorAggregateProjector.Incremental<Counter, CounterNumber, CounterChange> {

	override var projection: Counter? = null
		private set


	override fun add(event: RaptorAggregateEvent<CounterNumber, CounterChange>) =
		projection.apply(event).also { projection = it }


	private fun Counter?.apply(event: RaptorAggregateEvent<CounterNumber, CounterChange>): Counter? {
		if (this != null)
			check(id == event.aggregateId) { "Cannot apply event for aggregate ${event.aggregateId} to $id." }

		@Suppress("KotlinConstantConditions")
		return when (val change = event.change) {
			Created -> when (this) {
				null -> Counter(id = event.aggregateId, value = 0)
				else -> error("Cannot create aggregate $id multiple times.")
			}

			else -> when (this) {
				null -> error("Missing first event.")
				else -> when (change) {
					Created -> error("Compiler error.s")
					Incremented -> copy(value = value + 1)
				}
			}
		}
	}
}
