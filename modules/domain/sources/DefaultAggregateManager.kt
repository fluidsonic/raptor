package io.fluidsonic.raptor.domain

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*


// FIXME races, txs
internal class DefaultAggregateManager(
	private val definitions: RaptorAggregateDefinitions,
	private val eventFactory: RaptorAggregateEventFactory,
	private val eventStream: DefaultAggregateStream,
	private val fixme: DefaultAggregateProjectionLoaderManager, // FIXME
	private val projectionEventStream: DefaultAggregateProjectionStream,
	private val store: RaptorAggregateStore,
) : RaptorAggregateManager {

	private val controllers: MutableMap<RaptorAggregateId, RaptorAggregateController<*>> = hashMapOf()
	private val mutex = Mutex()
	private var pendingEvents: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf() // FIXME reset on transaction failure
	private var status = Status.new


	override suspend fun commit() {
		mutex.withLock {
			when (status) {
				Status.started, Status.stopping -> {}
				Status.new, Status.starting -> error("Cannot commit changes before aggregate manager was started.")
				Status.stopped -> error("Cannot commit changes after aggregate manager was stopped.")
			}

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


	override suspend fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>) {
		mutex.withLock {
			when (status) {
				Status.started, Status.stopping -> {}
				Status.new, Status.starting -> error("Cannot execute commands before aggregate manager was started.")
				Status.stopped -> error("Cannot execute commands after aggregate manager was stopped.")
			}

			pendingEvents += controller(id).execute(command)
		}
	}


	// FIXME use controller?
	private suspend fun process(batch: RaptorAggregateEventBatch<*, *>) {
		val projectionEvents = batch.events.mapNotNull { fixme.addEvent(it) }

		eventStream.emit(batch)

		if (projectionEvents.isNotEmpty()) {
			val lastProjectionEvent = projectionEvents.last()

			projectionEventStream.emit(RaptorAggregateProjectionEventBatch(
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


	override suspend fun start() {
		mutex.withLock {
			check(status == Status.new) { "Cannot start an aggregate manager that is $status." }
			status = Status.starting
		}

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

		mutex.withLock {
			status = Status.started

			// TODO Might have deadlock potential. Add buffer somewhere?
			eventStream.emit(RaptorAggregateStreamMessage.Loaded)
			projectionEventStream.emit(RaptorAggregateProjectionStreamMessage.Loaded)
		}
	}


	override suspend fun stop() {
		mutex.withLock {
			check(status == Status.started) { "Cannot stop an aggregate manager that is $status." }
			status = Status.stopping
		}

		// FIXME If we launch commands async in response to events this will stop the manager too early.
		coroutineScope {
			launch { eventStream.stop() }
			launch { projectionEventStream.stop() }
		}

		mutex.withLock {
			status = Status.stopped
		}
	}


	private enum class Status {

		new,
		started,
		starting,
		stopped,
		stopping,
	}
}
