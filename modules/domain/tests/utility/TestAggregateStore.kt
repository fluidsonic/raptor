import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.Timestamp
import kotlinx.coroutines.flow.*


class TestAggregateStore(
	events: List<RaptorAggregateEvent<*, *>> = emptyList(),
) : RaptorAggregateStore {

	private var batches: MutableList<List<RaptorAggregateEvent<*, *>>> = mutableListOf()
	private val events: MutableList<RaptorAggregateEvent<*, *>> = events.toMutableList()

	// Indexed by aggregate ID so `loadAggregate` looks up a single aggregate's history directly
	// instead of scanning every event of every aggregate in the store.
	private val eventsByAggregateId: MutableMap<RaptorAggregateId, MutableList<RaptorAggregateEvent<*, *>>> =
		this.events.groupByTo(hashMapOf()) { it.aggregateId }


	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		this.events += events
		batches += events

		for (event in events)
			eventsByAggregateId.getOrPut(event.aggregateId) { mutableListOf() }.add(event)
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


	override fun <Id : RaptorAggregateId> loadAggregate(
		definition: RaptorAggregateDefinition<*, out Id, *, *>,
		id: Id,
		afterVersion: Int?,
	): Flow<RaptorAggregateEvent<*, *>> {
		require(definition.idClass == id::class) {
			"`id` must be of type `${definition.idClass.qualifiedName}` for aggregate '${definition.discriminator}', but was `${id::class.qualifiedName}`: $id"
		}
		check(!definition.isIndividual) {
			"Cannot load events for individual aggregate '${definition.discriminator}' via `loadAggregate`; use its dedicated `RaptorIndividualAggregateStore` instead."
		}

		// Already ascending by version: events are appended to this list in `add()` call order, and
		// per-aggregate versions are assigned sequentially, so no separate sort is needed.
		val aggregateEvents = eventsByAggregateId[id] ?: return emptyFlow()

		return aggregateEvents
			.let { events -> if (afterVersion == null) events else events.filter { it.version > afterVersion } }
			.asFlow()
	}


	fun takeBatches(): List<List<RaptorAggregateEvent<*, *>>> {
		val batches = this.batches.toList()
		this.batches.clear()

		return batches
	}
}
