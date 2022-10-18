package io.fluidsonic.raptor.domain


// FIXME races, txs
internal class DefaultAggregateManager(
	private val definitions: RaptorAggregateDefinitions,
	private val eventFactory: RaptorAggregateEventFactory,
	private val eventStream: DefaultAggregateEventStream,
	private val fixme: DefaultAggregateProjectionLoaderManager, // FIXME
	private val projectionEventStream: DefaultAggregateProjectionEventStream,
	private val store: RaptorAggregateStore,
) : RaptorAggregateManager {

	private val controllers: MutableMap<RaptorAggregateId, RaptorAggregateController<*>> = hashMapOf()
	private var pendingEvents: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf() // FIXME reset on transaction failure


	override suspend fun commit() {
		if (pendingEvents.isEmpty())
			return

		val pendingEvents = pendingEvents
		this.pendingEvents = mutableListOf()

		store.add(pendingEvents) // FIXME copy?

		pendingEvents
			.groupBy { it.aggregateId }
			.values
			.map { events ->
				val lastEvent = events.last()

				RaptorAggregateEventBatch(
					aggregateId = lastEvent.aggregateId,
					events = when (events.size) {
						1 -> events
						else -> events.map { it.copy(lastVersionInBatch = lastEvent.version) }
					},
					isReplay = lastEvent.isReplay,
					version = lastEvent.version,
				)
			}
			.forEach { process(it) }
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Id : RaptorAggregateId> controller(id: Id): RaptorAggregateController<Id> =
		controllers.getOrPut(id) { createController(id) } as RaptorAggregateController<Id>


	@Suppress("UNCHECKED_CAST")
	private fun <Id : RaptorAggregateId> createController(id: Id): RaptorAggregateController<Id> =
		DefaultAggregateController(
			// Seriously? Isn't there a better way?
			definition = definitions[id] as RaptorAggregateDefinition<
				RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>,
				RaptorAggregateId,
				RaptorAggregateCommand<RaptorAggregateId>,
				RaptorAggregateChange<RaptorAggregateId>
				>, // FIXME null check
			eventFactory = eventFactory,
			id = id,
		) as RaptorAggregateController<Id>


	override fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>) {
		pendingEvents += controller(id).execute(command)
	}


	override suspend fun load() {
		// FIXME use controller?
		val eventsInBatchByAggregateId: MutableMap<RaptorAggregateId, MutableList<RaptorAggregateEvent<*, *>>> = hashMapOf()

		store.load().collect { event ->
			val eventsInBatch = eventsInBatchByAggregateId.getOrPut(event.aggregateId, ::mutableListOf)
			eventsInBatch += event

			controller(event.aggregateId).handle(event)

			if (event.version == event.lastVersionInBatch) {
				process(RaptorAggregateEventBatch(
					aggregateId = event.aggregateId,
					events = eventsInBatch,
					isReplay = event.isReplay,
					version = event.version,
				))

				eventsInBatchByAggregateId[event.aggregateId] = mutableListOf()
			}
		}
	}


	// FIXME use controller?
	private suspend fun process(batch: RaptorAggregateEventBatch<*, *>) {
		val projectionEvents = batch.events.mapNotNull { fixme.addEvent(it) }

		eventStream.add(batch)

		if (projectionEvents.isNotEmpty()) {
			val lastProjectionEvent = projectionEvents.last()

			projectionEventStream.add(RaptorAggregateProjectionEventBatch(
				events = when (projectionEvents.size) {
					1 -> projectionEvents
					else -> projectionEvents.map { it.copy(lastVersionInBatch = lastProjectionEvent.version) }
				},
				isReplay = lastProjectionEvent.isReplay,
				projectionId = lastProjectionEvent.projectionId,
				version = lastProjectionEvent.version,
			))
		}
	}
}
