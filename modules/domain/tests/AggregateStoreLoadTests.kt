import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*
import org.slf4j.helpers.*


private class LoadTestsIndividualId(private val value: String) : RaptorAggregateId {
	override fun toString() = value
}


// Individual aggregates are wired eagerly at `startIn`, which needs a `RaptorIndividualAggregateStoreFactory`
// binding regardless of whether any test actually exercises that store.
private class StubIndividualAggregateStoreFactory : RaptorIndividualAggregateStoreFactory {

	override fun create(name: String, eventType: KType): RaptorIndividualAggregateStore<*, *> =
		object : RaptorIndividualAggregateStore<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>> {
			override suspend fun lastEventId(): RaptorAggregateEventId? = null
			override suspend fun load(id: RaptorAggregateId) = emptyList<RaptorAggregateEvent<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>>()
			override suspend fun reload() = emptyList<RaptorAggregateEvent<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>>()
			override suspend fun save(id: RaptorAggregateId, events: List<RaptorAggregateEvent<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>>) {}
		}
}


private sealed interface LoadTestsIndividualChange : RaptorAggregateChange<LoadTestsIndividualId> {
	object Created : LoadTestsIndividualChange
}


private sealed interface LoadTestsIndividualCommand : RaptorAggregateCommand<LoadTestsIndividualId> {
	object Create : LoadTestsIndividualCommand
}


private class LoadTestsIndividualAggregate(
	override val id: LoadTestsIndividualId,
) : RaptorAggregate<LoadTestsIndividualId, LoadTestsIndividualCommand, LoadTestsIndividualChange> {

	override fun copy() = LoadTestsIndividualAggregate(id)
	override fun execute(command: LoadTestsIndividualCommand): List<LoadTestsIndividualChange> = listOf(LoadTestsIndividualChange.Created)
	override fun handle(change: LoadTestsIndividualChange) {}
}


class AggregateStoreLoadTests {

	private val bankAccountId1 = BankAccountNumber("1")
	private val bankAccountId2 = BankAccountNumber("2")
	private val counterId = CounterNumber("a")

	private val e1 = RaptorAggregateEvent(
		aggregateId = bankAccountId1,
		change = Created(owner = "Marc"),
		id = RaptorAggregateEventId(1),
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
		aggregateId = bankAccountId1,
		change = Deposited(amount = 5),
		id = RaptorAggregateEventId(4),
		timestamp = Timestamp.fromEpochSeconds(4),
		version = 3,
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
		includeIndividualAggregate: Boolean = false,
	) =
		raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorEventPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(ManualClock().also { it.set(Timestamp.fromEpochSeconds(0)) })
				provide<Logger>(NOPLogger.NOP_LOGGER)
			}

			domain.aggregates {
				individualStoreFactory(StubIndividualAggregateStoreFactory())
				store(store)

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<Create>()
					command<Delete>()
					command<Deposit>()
					command<Label>()
					command<Withdraw>()

					change<Created>("created")
					change<Deleted>("deleted")
					change<Deposited>("deposited")
					change<Labeled>("labeled")
					change<Withdrawn>("withdrawn")
				}

				new(::CounterAggregate, "counter") {
					project(::CounterProjector)

					command<CounterCommand.Create>()
					command<CounterCommand.Increment>()

					change<CounterChange.Created>("created")
					change<CounterChange.Incremented>("incremented")
				}

				if (includeIndividualAggregate)
					new(::LoadTestsIndividualAggregate, "load tests individual thing", individual = true) {}
			}
		}


	private fun <Id : RaptorAggregateId> definitionFor(raptor: Raptor, id: Id) =
		raptor.context.asScope().di.get<RaptorAggregateDefinitions>()[id]!!


	@Test
	fun testOnlyRequestedAggregateEventsAreReturned() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, counterId)
		val events = store.loadAggregate(definition, counterId).toList()

		assertEquals(actual = events, expected = listOf(e2))

		raptor.lifecycle.stop()
	}


	@Test
	fun testDifferentIdsOfSameTypeDoNotCrossContaminate() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)
		val events1 = store.loadAggregate(definition, bankAccountId1).toList()
		val events2 = store.loadAggregate(definition, bankAccountId2).toList()

		assertEquals(actual = events1, expected = listOf(e1, e3, e4))
		assertEquals(actual = events2, expected = listOf(e5))

		raptor.lifecycle.stop()
	}


	@Test
	fun testAfterVersionResumesExclusivelyAndNullYieldsFullHistory() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)
		val fromStart = store.loadAggregate(definition, bankAccountId1, afterVersion = null).toList()
		val resumed = store.loadAggregate(definition, bankAccountId1, afterVersion = 2).toList()

		assertEquals(actual = fromStart, expected = listOf(e1, e3, e4))
		assertEquals(actual = resumed, expected = listOf(e4))

		raptor.lifecycle.stop()
	}


	@Test
	fun testEventsArriveInAscendingVersionOrder() = runTest {
		val store = TestAggregateStore()
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)

		// Added across separate commits, in the ascending version order a real command execution
		// always produces for one aggregate — the store relies on this append order rather than
		// re-sorting on every read.
		store.add(listOf(e1))
		store.add(listOf(e3))
		store.add(listOf(e4))

		val events = store.loadAggregate(definition, bankAccountId1).toList()

		assertEquals(actual = events.map { it.version }, expected = listOf(1, 2, 3))

		raptor.lifecycle.stop()
	}


	@Test
	fun testUnknownAggregateIdReturnsEmptyHistory() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)
		val events = store.loadAggregate(definition, BankAccountNumber("never-added")).toList()

		assertEquals(actual = events, expected = emptyList())

		raptor.lifecycle.stop()
	}


	@Test
	fun testAfterVersionAtOrAboveHighestVersionReturnsEmpty() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)
		val events = store.loadAggregate(definition, bankAccountId1, afterVersion = 3).toList()

		assertEquals(actual = events, expected = emptyList())

		raptor.lifecycle.stop()
	}


	@Test
	fun testAfterVersionZeroReturnsFullHistory() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val definition = definitionFor(raptor, bankAccountId1)
		val events = store.loadAggregate(definition, bankAccountId1, afterVersion = 0).toList()

		assertEquals(actual = events, expected = listOf(e1, e3, e4))

		raptor.lifecycle.stop()
	}


	@Test
	fun testSingleBatchWithMultipleAggregateIdsIndexesEachSeparately() = runTest {
		val store = TestAggregateStore()
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		// One commit batch spanning two different aggregates, as a real command execution would produce.
		store.add(listOf(e1, e2))

		val bankDefinition = definitionFor(raptor, bankAccountId1)
		val counterDefinition = definitionFor(raptor, counterId)

		assertEquals(actual = store.loadAggregate(bankDefinition, bankAccountId1).toList(), expected = listOf(e1))
		assertEquals(actual = store.loadAggregate(counterDefinition, counterId).toList(), expected = listOf(e2))

		raptor.lifecycle.stop()
	}


	@Test
	fun testMismatchedDefinitionAndIdAcrossRegisteredTypesFailsLoudly() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val bankDefinition = definitionFor(raptor, bankAccountId1)

		// `Id` infers to the common supertype `RaptorAggregateId`, so this compiles even though
		// `bankDefinition` and `counterId` belong to different registered aggregate types — only the
		// runtime check actually rejects the mismatch.
		assertFailsWith<IllegalArgumentException> {
			store.loadAggregate<RaptorAggregateId>(bankDefinition, counterId)
		}

		raptor.lifecycle.stop()
	}


	@Test
	fun testIndividualAggregateFailsLoudly() = runTest {
		val store = TestAggregateStore()
		val raptor = buildRaptor(store, includeIndividualAggregate = true)
		raptor.lifecycle.startIn(this)

		val id = LoadTestsIndividualId("x")
		val definition = definitionFor(raptor, id)

		assertFailsWith<IllegalStateException> {
			store.loadAggregate(definition, id)
		}

		raptor.lifecycle.stop()
	}
}
