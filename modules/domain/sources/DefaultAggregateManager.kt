package io.fluidsonic.raptor.cqrs


// FIXME races, txs
internal class DefaultAggregateManager(
	private val domain: RaptorDomain,
	private val eventFactory: RaptorAggregateEventFactory,
	private val fixme: DefaultAggregateProjectionLoaderManager, // FIXME
) : RaptorAggregateManager {

	private val controllers: MutableMap<RaptorAggregateId, RaptorAggregateController<*>> = hashMapOf()
	private var pendingEvents: MutableList<RaptorEvent<*, *>> = mutableListOf()


	override suspend fun commit() {
		if (pendingEvents.isEmpty())
			return

		val pendingEvents = pendingEvents
		this.pendingEvents = mutableListOf()

		domain.aggregates.store.add(pendingEvents) // FIXME copy?

		for (event in pendingEvents)
			fixme.addEvent(event)

		// FIXME event bus
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Id : RaptorAggregateId> controller(id: Id): RaptorAggregateController<Id> =
		controllers.getOrPut(id) { createController(id) } as RaptorAggregateController<Id>


	@Suppress("UNCHECKED_CAST")
	private fun <Id : RaptorAggregateId> createController(id: Id): RaptorAggregateController<Id> =
		DefaultAggregateController(
			// Seriously? Isn't there a better way?
			definition = domain.aggregates.definition(id) as RaptorAggregateDefinition<
				RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateEvent<RaptorAggregateId>>,
				RaptorAggregateId,
				RaptorAggregateCommand<RaptorAggregateId>,
				RaptorAggregateEvent<RaptorAggregateId>
				>,
			eventFactory = eventFactory,
			id = id,
		) as RaptorAggregateController<Id>


	override fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>) {
		pendingEvents += controller(id).execute(command)
	}


	override suspend fun load() {
		domain.aggregates.store.load().collect { event ->
			controller(event.aggregateId).handle(event)
			fixme.addEvent(event)
		}
	}
}
