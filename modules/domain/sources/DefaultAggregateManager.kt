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

		for (event in pendingEvents) {
			val projectionEvent = fixme.addEvent(event)

			eventStream.add(event)

			if (projectionEvent != null)
				projectionEventStream.add(projectionEvent)
		}
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
		store.load().collect { event ->
			controller(event.aggregateId).handle(event)
			fixme.addEvent(event)
		}
	}
}
