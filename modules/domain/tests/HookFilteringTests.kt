import BankAccountChange.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*
import org.slf4j.helpers.*


// Implemented by both BankAccountNumber and CounterNumber, purely to exercise is-a filter resolution
// against a sealed supertype spanning multiple registered aggregate ID classes.
interface FilterMarkerAggregateId : RaptorAggregateId


private class IndividualThingId(private val value: String) : RaptorAggregateId {
	override fun toString() = value
}


private sealed interface IndividualThingChange : RaptorAggregateChange<IndividualThingId> {
	object Created : IndividualThingChange
}


private sealed interface IndividualThingCommand : RaptorAggregateCommand<IndividualThingId> {
	object Create : IndividualThingCommand
}


private class IndividualThingAggregate(
	override val id: IndividualThingId,
) : RaptorAggregate<IndividualThingId, IndividualThingCommand, IndividualThingChange> {

	override fun copy() = IndividualThingAggregate(id)
	override fun execute(command: IndividualThingCommand): List<IndividualThingChange> = listOf(IndividualThingChange.Created)
	override fun handle(change: IndividualThingChange) {}
}


// Deliberately overrides equals()/hashCode() by `label` alone, independent of `aggregateIdClassFilter` —
// two instances sharing a label but declaring different filters must still be dispatched independently.
// A filter-resolution cache keyed by the hook itself (e.g. a Map<RaptorDomainStreamHook, ...>) would
// collapse them into one entry and silently apply one hook's filter to both.
private class EqualityHook(
	private val label: String,
	override val aggregateIdClassFilter: Set<KClass<out RaptorAggregateId>>?,
) : RaptorDomainStreamHook {

	val messages = mutableListOf<RaptorAggregateStreamMessage<*, *>>()


	override fun onAggregateStreamMessage(message: RaptorAggregateStreamMessage<*, *>) {
		messages += message
	}


	override fun equals(other: Any?) =
		other is EqualityHook && other.label == label


	override fun hashCode() =
		label.hashCode()
}


class HookFilteringTests {

	private val bankAccountId1 = BankAccountNumber("1")
	private val bankAccountId2 = BankAccountNumber("2")
	private val counterId = CounterNumber("a")

	private val e1 = RaptorAggregateEvent(
		aggregateId = bankAccountId1,
		change = Created(owner = "Marc"),
		id = RaptorAggregateEventId(1),
		lastVersionInBatch = 2,
		timestamp = Timestamp.fromEpochSeconds(1),
		version = 1,
	)
	private val e2 = RaptorAggregateEvent(
		aggregateId = counterId,
		change = CounterChange.Created,
		id = RaptorAggregateEventId(2),
		timestamp = Timestamp.fromEpochSeconds(2),
		version = 1,
	)
	private val e3 = RaptorAggregateEvent(
		aggregateId = bankAccountId1,
		change = Deposited(amount = 10),
		id = RaptorAggregateEventId(3),
		timestamp = Timestamp.fromEpochSeconds(3),
		version = 2,
	)
	private val e4 = RaptorAggregateEvent(
		aggregateId = counterId,
		change = CounterChange.Incremented,
		id = RaptorAggregateEventId(4),
		timestamp = Timestamp.fromEpochSeconds(4),
		version = 2,
	)
	private val e5 = RaptorAggregateEvent(
		aggregateId = bankAccountId2,
		change = Created(owner = "Someone"),
		id = RaptorAggregateEventId(5),
		timestamp = Timestamp.fromEpochSeconds(5),
		version = 1,
	)

	private fun seedEvents(): List<RaptorAggregateEvent<*, *>> =
		listOf(e1, e2, e3, e4, e5)


	private suspend fun TestScope.buildRaptor(
		store: TestAggregateStore,
		hooks: List<RaptorDomainStreamHook>,
		includeIndividualAggregate: Boolean = false,
	) =
		raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(ManualClock().also { it.set(Timestamp.fromEpochSeconds(0)) })
				provide<Logger>(NOPLogger.NOP_LOGGER)
			}

			domain.aggregates {
				store(store)

				for (h in hooks)
					hook { h }

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<BankAccountCommand.Create>()
					command<BankAccountCommand.Delete>()
					command<BankAccountCommand.Deposit>()
					command<BankAccountCommand.Label>()
					command<BankAccountCommand.Withdraw>()

					change<BankAccountChange.Created>("created")
					change<BankAccountChange.Deleted>("deleted")
					change<BankAccountChange.Deposited>("deposited")
					change<BankAccountChange.Labeled>("labeled")
					change<BankAccountChange.Withdrawn>("withdrawn")
				}

				new(::CounterAggregate, "counter") {
					project(::CounterProjector)

					command<CounterCommand.Create>()
					command<CounterCommand.Increment>()

					change<CounterChange.Created>("created")
					change<CounterChange.Incremented>("incremented")
				}

				if (includeIndividualAggregate)
					new(::IndividualThingAggregate, "individual thing", individual = true)
			}
		}


	// Runs startIn in a scope detached from the test's own job: DefaultLifecycle.startIn attaches a
	// SupervisorJob to the given scope before running start actions, and never completes it when a start
	// action throws (as our validation errors do), which would otherwise hang runTest waiting for it.
	private suspend fun TestScope.startExpectingFailure(raptor: Raptor): IllegalStateException {
		val startScope = CoroutineScope(coroutineContext + Job())

		val exception = assertFailsWith<IllegalStateException> {
			raptor.lifecycle.startIn(startScope)
		}
		startScope.cancel()

		return exception
	}


	@Test
	fun testUnfilteredHooksReceiveIdenticalList() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook1 = MessageCollectionHook()
		val hook2 = MessageCollectionHook()

		val raptor = buildRaptor(store, listOf(hook1, hook2))
		raptor.lifecycle.startIn(this)

		val replay1 = hook1.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		val replay2 = hook2.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()

		assertSame(actual = replay2.batches, expected = replay1.batches)

		raptor.lifecycle.stop()
	}


	@Test
	fun testSingleClassFilterReceivesOnlyOwnBatches() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = setOf(CounterNumber::class))

		val raptor = buildRaptor(store, listOf(hook))
		raptor.lifecycle.startIn(this)

		val replay = hook.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		assertEquals(
			actual = replay.batches,
			expected = listOf(
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e2), version = 1),
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e4), version = 2),
			),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testMultiClassFilterPreservesGlobalOrder() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = setOf(BankAccountNumber::class, CounterNumber::class))

		val raptor = buildRaptor(store, listOf(hook))
		raptor.lifecycle.startIn(this)

		val replay = hook.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		assertEquals(
			actual = replay.batches,
			expected = listOf(
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e2), version = 1),
				RaptorAggregateEventBatch(aggregateId = bankAccountId1, events = listOf(e1, e3), version = 2),
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e4), version = 2),
				RaptorAggregateEventBatch(aggregateId = bankAccountId2, events = listOf(e5), version = 1),
			),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testEmptyFilterSkipsReplay() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = emptySet(), projectionIdClassFilter = emptySet())

		val raptor = buildRaptor(store, listOf(hook))
		raptor.lifecycle.startIn(this)

		assertEquals<List<RaptorAggregateStreamMessage<*, *>>>(actual = hook.messages, expected = listOf(RaptorAggregateStreamMessage.Loaded))
		assertEquals<List<RaptorAggregateProjectionStreamMessage<*, *, *>>>(
			actual = hook.projectionMessages,
			expected = listOf(RaptorAggregateProjectionStreamMessage.Loaded),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testUnknownFilterClassFailsAtStartup() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = setOf(IndividualThingId::class))

		val raptor = buildRaptor(store, listOf(hook))

		val exception = startExpectingFailure(raptor)
		assertContains(exception.message.orEmpty(), "MessageCollectionHook")
		assertContains(exception.message.orEmpty(), "IndividualThingId")
	}


	@Test
	fun testSupertypeFilterMatchesSubtypes() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = setOf(FilterMarkerAggregateId::class))

		val raptor = buildRaptor(store, listOf(hook))
		raptor.lifecycle.startIn(this)

		val replay = hook.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		assertEquals(
			actual = replay.batches,
			expected = listOf(
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e2), version = 1),
				RaptorAggregateEventBatch(aggregateId = bankAccountId1, events = listOf(e1, e3), version = 2),
				RaptorAggregateEventBatch(aggregateId = counterId, events = listOf(e4), version = 2),
				RaptorAggregateEventBatch(aggregateId = bankAccountId2, events = listOf(e5), version = 1),
			),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testIndividualAggregateFilterRejected() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(aggregateIdClassFilter = setOf(IndividualThingId::class))

		val raptor = buildRaptor(store, listOf(hook), includeIndividualAggregate = true)

		val exception = startExpectingFailure(raptor)
		assertContains(exception.message.orEmpty(), "MessageCollectionHook")
		assertContains(exception.message.orEmpty(), "IndividualThingId")
	}


	@Test
	fun testLiveCommitRespectsFilter() = runTest {
		val store = TestAggregateStore()
		val bankHook = MessageCollectionHook(aggregateIdClassFilter = setOf(BankAccountNumber::class))
		val counterHook = MessageCollectionHook(aggregateIdClassFilter = setOf(CounterNumber::class))

		val raptor = buildRaptor(store, listOf(bankHook, counterHook))
		raptor.lifecycle.startIn(this)

		with(raptor.context.asScope()) {
			commandExecutor.execute(bankAccountId1, BankAccountCommand.Create("Marc"))
		}

		val bankBatches = bankHook.messages.filterIsInstance<RaptorAggregateEventBatch<*, *>>()
		val counterBatches = counterHook.messages.filterIsInstance<RaptorAggregateEventBatch<*, *>>()

		assertEquals(actual = bankBatches.map { it.aggregateId }, expected = listOf(bankAccountId1))
		assertEquals(actual = counterBatches, expected = emptyList())

		raptor.lifecycle.stop()
	}


	@Test
	fun testProjectionFilterIndependentOfAggregateFilter() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook = MessageCollectionHook(
			aggregateIdClassFilter = emptySet(),
			projectionIdClassFilter = setOf(BankAccountNumber::class),
		)

		val raptor = buildRaptor(store, listOf(hook))
		raptor.lifecycle.startIn(this)

		assertEquals<List<RaptorAggregateStreamMessage<*, *>>>(actual = hook.messages, expected = listOf(RaptorAggregateStreamMessage.Loaded))

		val projectionReplay = hook.projectionMessages.filterIsInstance<RaptorAggregateProjectionStreamMessage.Replay>().single()
		assertEquals(
			actual = projectionReplay.batches.map { it.projectionId },
			expected = listOf(bankAccountId1, bankAccountId2),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testSharedFilterSetSharesList() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val hook1 = MessageCollectionHook(aggregateIdClassFilter = setOf(BankAccountNumber::class, CounterNumber::class))
		val hook2 = MessageCollectionHook(aggregateIdClassFilter = setOf(BankAccountNumber::class, CounterNumber::class))

		val raptor = buildRaptor(store, listOf(hook1, hook2))
		raptor.lifecycle.startIn(this)

		val replay1 = hook1.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		val replay2 = hook2.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()

		assertSame(actual = replay2.batches, expected = replay1.batches)

		raptor.lifecycle.stop()
	}


	@Test
	fun testHooksWithCollidingEqualityAreDispatchedIndependently() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val bankHook = EqualityHook(label = "shared", aggregateIdClassFilter = setOf(BankAccountNumber::class))
		val counterHook = EqualityHook(label = "shared", aggregateIdClassFilter = setOf(CounterNumber::class))

		val raptor = buildRaptor(store, listOf(bankHook, counterHook))
		raptor.lifecycle.startIn(this)

		val bankReplay = bankHook.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()
		val counterReplay = counterHook.messages.filterIsInstance<RaptorAggregateStreamMessage.Replay>().single()

		assertEquals(actual = bankReplay.batches.map { it.aggregateId }, expected = listOf(bankAccountId1, bankAccountId2))
		assertEquals(actual = counterReplay.batches.map { it.aggregateId }, expected = listOf(counterId, counterId))

		raptor.lifecycle.stop()
	}
}
