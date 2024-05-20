import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.Timestamp
import kotlinx.coroutines.flow.*


class TestAggregateStore(
	events: List<RaptorAggregateEvent<*, *>> = emptyList(),
) : RaptorAggregateStore {

	private var batches: MutableList<List<RaptorAggregateEvent<*, *>>> = mutableListOf()
	private val events: MutableList<RaptorAggregateEvent<*, *>> = events.toMutableList()


	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		this.events += events

		batches += events
	}


	override suspend fun lastEventTimestampOrNull(): Timestamp? =
		events.lastOrNull()?.timestamp


	override fun load(after: RaptorAggregateEventId?): Flow<RaptorAggregateEvent<*, *>> =
		events
			.let { events ->
				when (after) {
					null -> events.toList()
					else -> events.filter { it.id > after }
				}
			}
			.asFlow()


	fun takeBatches(): List<List<RaptorAggregateEvent<*, *>>> {
		val batches = this.batches.toList()
		this.batches.clear()

		return batches
	}
}
