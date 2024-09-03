package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.time.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import kotlinx.datetime.*


internal class DefaultAggregateManager(
	private val clock: Clock,
	private val context: RaptorContext,
	private val definitions: RaptorAggregateDefinitions,
	private val eventEmitter: RaptorEventEmitter,
	private val eventSource: RaptorEventSource,
	private val onCommittedActions: List<suspend RaptorScope.() -> Unit>,
	private val projectionLoaderManager: DefaultAggregateProjectionLoaderManager, // TODO Hack.
	private val store: RaptorAggregateStore,
) : RaptorAggregateCommandExecutor, RaptorAggregateProvider, RaptorDomain {

	private val aggregateStates: MutableMap<RaptorAggregateId, AggregateState<*>> = hashMapOf()
	private val mutex = Mutex()
	private var nextEventId = 1L
	private var status = atomic(Status.new)
	private val stopJobs = mutableListOf<Job>()

	override val loaded = CompletableDeferred<RaptorDomain>()


	override fun execution(): RaptorAggregateCommandExecution {
		when (status.value) {
			Status.started, Status.stopping -> {}
			Status.new, Status.starting -> error("Cannot execute commands before aggregate manager was started.")
			Status.stopped -> error("Cannot execute commands after aggregate manager was stopped.")
		}

		return Execution(manager = this)
	}


	// TODO Once supporting multiple instances: always auto-retry if the conflict is due to an event ID conflict.
	@Suppress("UNCHECKED_CAST")
	private suspend inline fun commit(action: Commit.() -> Unit) {
		mutex.withLock {
			when (status.value) {
				Status.started, Status.stopping -> {}
				Status.new, Status.starting -> error("Cannot commit changes before aggregate manager was started.")
				Status.stopped -> error("Cannot commit changes after aggregate manager was stopped.")
			}

			val commit = Commit(
				aggregateStates = aggregateStates,
				definitions = definitions,
				nextEventId = nextEventId,
				timestamp = clock.now(),
			).apply(action)

			if (commit.events.isEmpty())
				return

			// TODO If the store operation succeeds but we lose connection we end up in an unrecoverable state.
			//      Events were not dispatched and nextEventId is incorrect.
			store.add(commit.events)

			nextEventId = commit.events
				.maxOf { it.id.toLong() }
				.plus(1)

			for (event in commit.events) {
				val aggregate = checkNotNull(commit.aggregates[event.aggregateId])
					as RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>
				val id = aggregate.id

				aggregateStates.compute(id) { _, state ->
					state
						?.let { it as AggregateState<RaptorAggregateId> }
						// Not actually the correct aggregate for this version, but subsequent events in the same commit will fix it.
						// TODO Improve performance by updating the state only once per aggregate.
						?.copy(aggregate = aggregate, version = event.version)
						?: AggregateState(
							aggregate = aggregate,
							definition = checkNotNull(definitions[id]) as RaptorAggregateDefinition<RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>, RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>,
							version = event.version,
						)
				}
			}

			for (batch in commit.events)
				process(batch)
		}

		for (onCommitted in onCommittedActions)
			onCommitted(context)
	}


	private suspend fun process(event: RaptorAggregateEvent<*, *>) {
		// Make sure that the projection is updated before we emit any events.
		val projectionEvent = projectionLoaderManager.addEvent(event)

		// FIXME Emit in parallel.
		eventEmitter.emit(event)

		if (projectionEvent != null)
			eventEmitter.emit(projectionEvent)
	}


	@Suppress("UNCHECKED_CAST")
	override suspend fun <Id : RaptorAggregateId> provide(
		id: Id,
	): Pair<RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>>, Int> =
		mutex.withLock {
			when (status.value) {
				Status.started, Status.stopping -> {}
				Status.new, Status.starting -> error("Cannot provide aggregates before aggregate manager was started.")
				Status.stopped -> error("Cannot provide aggregates after aggregate manager was stopped.")
			}

			val state = aggregateStates[id] as AggregateState<Id>?
			val aggregate = state?.aggregate
				?.copy()
				?: definitions.create(id)?.let { it as RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>> }
				?: error("There's no aggregate definition for ID $id (${id::class.qualifiedName}).")
			val version = state?.version ?: 0

			aggregate to version
		}


	@Suppress("UNCHECKED_CAST")
	suspend fun start(
		individualManagers: Collection<DefaultIndividualAggregateManager<*, *>>,
	) {
		check(status.compareAndSet(Status.new, Status.starting)) { "Cannot start an aggregate manager that is $status." }

		// FIXME implement dispatching to listeners
//		stopJobs += eventSource.subscribeIn(scope, ::onAggregateEvent)
//		stopJobs += eventSource.subscribeIn(scope, ::onAggregateProjectionEvent)

		var lastEventId = 0L

		store.load()
			.buffer(capacity = 2_000_000)
			.collect { event ->
				check(event.id.toLong() == lastEventId + 1) {
					when (lastEventId) {
						0L -> "Expected first aggregate event to have ID 1: $event"
						else -> "There's a gap in IDs between event $lastEventId and event ${event.id}."
					}
				}
				lastEventId = event.id.toLong()

				val id = event.aggregateId

				val state = aggregateStates.getOrPut(id) {
					val definition =
						checkNotNull(definitions[id]) as RaptorAggregateDefinition<
							RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>,
							RaptorAggregateId,
							RaptorAggregateCommand<RaptorAggregateId>,
							RaptorAggregateChange<RaptorAggregateId>
							>

					AggregateState(aggregate = definition.factory.create(id), definition = definition, version = 0)
				} as AggregateState<RaptorAggregateId>

				state.addEvent(event)
				process(event)
			}

		nextEventId = lastEventId + 1

		for (manager in individualManagers)
			manager.load()

		mutex.withLock {
			status.value = Status.started

			loaded.complete(this)
			eventEmitter.emit(RaptorAggregateReplayCompletedEvent)
		}
	}


	suspend fun stop() {
		check(status.compareAndSet(Status.started, Status.stopping)) { "Cannot stop an aggregate manager that is $status." }

		for (job in stopJobs)
			job.cancel()

		mutex.withLock {
			status.value = Status.stopped
		}
	}


	private data class AggregateState<Id : RaptorAggregateId>(
		val aggregate: RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>>,
		val definition: RaptorAggregateDefinition<RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>>, Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>>,
		var version: Int,
	) {

		fun addEvent(event: RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>) {
			check(event.version == version + 1) {
				when {
					event.version <= version -> "Cannot apply multiple events with the same version to aggregate '${aggregate.id.debug}':\n$event"
					else -> "Cannot apply events out of order (expected version ${version + 1}, " +
						"got ${event.version}) to aggregate '${aggregate.id.debug}': $event"
				}
			}

			aggregate.handle(event.change)
			version = event.version
		}
	}


	private class Commit(
		private val aggregateStates: Map<RaptorAggregateId, AggregateState<*>>,
		private val definitions: RaptorAggregateDefinitions,
		private var nextEventId: Long,
		private val timestamp: Timestamp,
	) {

		val aggregates: MutableMap<RaptorAggregateId, RaptorAggregate<*, *, *>> = hashMapOf()
		val events: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf()


		// TODO Probably some incorrect generic casts here. How to make more type-safe?
		@Suppress("UNCHECKED_CAST")
		fun <Id : RaptorAggregateId> add(id: Id, expectedVersion: Int?, commands: List<RaptorAggregateCommand<Id>>) {
			check(!aggregates.containsKey(id)) { "Cannot add commands for aggregate ${id.debug} multiple times." }

			val definition = definitions[id]
				as RaptorAggregateDefinition<out RaptorAggregate<Id, *, *>, Id, *, *>?
				?: error("There's no aggregate definition for ID $id (${id::class.qualifiedName}).")

			check(!definition.isIndividual) { "Can't use command execution for individual aggregates." }

			val state = aggregateStates[id] as AggregateState<Id>?
			val aggregate = state?.aggregate?.copy()
				?: definition.factory.create(id).let { it as RaptorAggregate<Id, RaptorAggregateCommand<Id>, RaptorAggregateChange<Id>> }
			val version = state?.version ?: 0

			if (expectedVersion != null && version != expectedVersion)
				throw RaptorAggregateVersionConflict(
					"Expected aggregate ${id.debug} at version $expectedVersion but encountered version $version.",
				)

			val changes = commands
				.flatMap { command ->
					try {
						aggregate.execute(command)
					}
					catch (e: Throwable) {
						throw RuntimeException("Failed executing command on aggregate ${id.debug}: $command", e)
					}.onEach { change ->
						try {
							aggregate.handle(change)
						}
						catch (e: Throwable) {
							throw RuntimeException("Failed handling change to aggregate ${id.debug}: $change", e)
						}
					}
				}
				.ifEmpty { return }

			val lastVersionInBatch = version + changes.size

			aggregates[id] = aggregate

			changes.mapIndexedTo(events) { index, change ->
				RaptorAggregateEvent(
					aggregateId = id,
					change = change,
					id = RaptorAggregateEventId(nextEventId++),
					timestamp = timestamp,
					version = version + index + 1,
					lastVersionInBatch = lastVersionInBatch,
				)
			}
		}
	}


	private class Execution(
		private val manager: DefaultAggregateManager,
	) : RaptorAggregateCommandExecution {

		private val batchByAggregateId: MutableMap<RaptorAggregateId, AggregateBatch<RaptorAggregateId>> = linkedMapOf()


		override suspend fun commit() {
			if (batchByAggregateId.isEmpty())
				return

			manager.commit {
				for (batch in batchByAggregateId.values)
					add(id = batch.id, expectedVersion = batch.expectedVersion, commands = batch.commands)
			}

			batchByAggregateId.clear()
		}


		@Suppress("UNCHECKED_CAST")
		override fun <Id : RaptorAggregateId> execute(id: Id, version: Int?, command: RaptorAggregateCommand<Id>) {
			batchByAggregateId.getOrPut(id) { AggregateBatch(id) }
				.let { it as AggregateBatch<Id> }
				.execute(command = command, version = version)
		}


		private class AggregateBatch<Id : RaptorAggregateId>(val id: Id) {

			val commands: MutableList<RaptorAggregateCommand<Id>> = mutableListOf()
			var expectedVersion: Int? = null


			fun execute(command: RaptorAggregateCommand<Id>, version: Int?) {
				if (version != null) {
					require(version >= 0) { "`version` must not be negative." }

					val expectedVersion = this.expectedVersion
					if (version != expectedVersion) {
						require(expectedVersion == null) {
							"`version` cannot be different for the same aggregate in the same batch.\n" +
								"Aggregate: $id (${id::class.qualifiedName})\n" +
								"Versions:  $expectedVersion & $version\n" +
								"Command:   $command"
						}

						this.expectedVersion = version
					}
				}

				commands += command
			}
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


@RaptorDsl
internal val RaptorScope.aggregateManager: DefaultAggregateManager
	get() = di.get()
