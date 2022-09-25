import io.fluidsonic.raptor.cqrs.*
import kotlinx.coroutines.flow.*


class TestAggregateStore(
	events: List<RaptorEvent<*, *>> = emptyList(),
) : RaptorAggregateStore {

	private var batches: MutableList<List<RaptorEvent<*, *>>> = mutableListOf()
	private val events: MutableList<RaptorEvent<*, *>> = events.toMutableList()


	override suspend fun add(events: List<RaptorEvent<*, *>>) {
		this.events += events

		batches += events
	}


	override fun load() =
		events.toList().asFlow()


	fun takeBatches(): List<List<RaptorEvent<*, *>>> {
		val batches = this.batches.toList()
		this.batches.clear()

		return batches
	}
}
