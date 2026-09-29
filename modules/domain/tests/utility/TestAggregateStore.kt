import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.Timestamp
import kotlin.reflect.*
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


	override fun loadPage(
		limit: Int,
		changes: Map<RaptorAggregateDefinition<*, *, *, *>, Set<RaptorAggregateChangeDefinition<*, *>>?>?,
		before: RaptorAggregateEventId?,
		after: RaptorAggregateEventId?,
		descending: Boolean,
	): Flow<RaptorAggregateEvent<*, *>> {
		require(limit in 1..RaptorAggregateLoader.MAX_PAGE_SIZE) {
			"`limit` must be in 1..${RaptorAggregateLoader.MAX_PAGE_SIZE}, but was $limit."
		}
		require(before == null || after == null || before > after) {
			"`before` ($before) must be greater than `after` ($after)."
		}

		val changeClassesByIdClass = changes?.entries?.associate { (definition, changeDefinitions) ->
			check(!definition.isIndividual) {
				"Cannot load events for individual aggregate '${definition.discriminator}' via `loadPage`; use its dedicated `RaptorIndividualAggregateStore` instead."
			}
			require(changeDefinitions == null || changeDefinitions.all { changeDefinition -> definition.changeDefinitions.any { it == changeDefinition } }) {
				"`changes` contains a change definition that does not belong to aggregate '${definition.discriminator}'."
			}

			definition.idClass to changeDefinitions?.mapTo(hashSetOf<KClass<*>>()) { it.changeClass }
		}

		return (if (descending) events.asReversed() else events)
			.asSequence()
			.filter { before == null || it.id < before }
			.filter { after == null || it.id > after }
			.filter { event ->
				changeClassesByIdClass == null ||
					changeClassesByIdClass.containsKey(event.aggregateId::class) &&
					changeClassesByIdClass[event.aggregateId::class].let { it == null || event.change::class in it }
			}
			.take(limit)
			.toList()
			.asFlow()
	}


	fun takeBatches(): List<List<RaptorAggregateEvent<*, *>>> {
		val batches = this.batches.toList()
		this.batches.clear()

		return batches
	}
}
