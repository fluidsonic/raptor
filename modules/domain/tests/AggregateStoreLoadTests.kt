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


private class LoadTestsNestedId(private val value: String) : RaptorAggregateId {
	override fun toString() = value
}


// Mirrors a change hierarchy where the registered leaf sits below intermediate sealed types.
private sealed interface LoadTestsNestedChange : RaptorAggregateChange<LoadTestsNestedId> {
	sealed interface Outer : LoadTestsNestedChange {
		sealed interface Inner : Outer {
			object Leaf : Inner
		}
	}
}


private sealed interface LoadTestsNestedCommand : RaptorAggregateCommand<LoadTestsNestedId> {
	object Create : LoadTestsNestedCommand
}


private class LoadTestsNestedAggregate(
	override val id: LoadTestsNestedId,
) : RaptorAggregate<LoadTestsNestedId, LoadTestsNestedCommand, LoadTestsNestedChange> {

	override fun copy() = LoadTestsNestedAggregate(id)
	override fun execute(command: LoadTestsNestedCommand): List<LoadTestsNestedChange> = listOf(LoadTestsNestedChange.Outer.Inner.Leaf)
	override fun handle(change: LoadTestsNestedChange) {}
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
		includeNestedAggregate: Boolean = false,
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

				if (includeNestedAggregate)
					new(::LoadTestsNestedAggregate, "load tests nested thing") {
						command<LoadTestsNestedCommand.Create>()

						change<LoadTestsNestedChange.Outer.Inner.Leaf>("leaf")
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


	private fun page(vararg events: RaptorAggregateEvent<*, *>) = events.toList()


	// Arguments are validated on the call itself, so a failing call throws here before anything is collected.
	private suspend fun RaptorScope.loadEvents(
		limit: Int,
		changes: Map<KClass<out RaptorAggregateId>, Set<KClass<out RaptorAggregateChange<*>>>?>? = null,
		before: RaptorAggregateEventId? = null,
		after: RaptorAggregateEventId? = null,
		descending: Boolean = true,
	) =
		loadAggregateEvents(limit = limit, changes = changes, before = before, after = after, descending = descending).toList()


	@Test
	fun testLoadPageDescendingPagesBackToFirstEvent() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		val page1 = scope.loadEvents(limit = 2)
		assertEquals(actual = page1, expected = page(e5, e4))

		val page2 = scope.loadEvents(limit = 2, before = page1.last().id)
		assertEquals(actual = page2, expected = page(e3, e2))

		val page3 = scope.loadEvents(limit = 2, before = page2.last().id)
		assertEquals(actual = page3, expected = page(e1))

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageAscendingPagesForward() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		val page1 = scope.loadEvents(limit = 2, descending = false)
		assertEquals(actual = page1, expected = page(e1, e2))

		val page2 = scope.loadEvents(limit = 2, descending = false, after = page1.last().id)
		assertEquals(actual = page2, expected = page(e3, e4))

		val page3 = scope.loadEvents(limit = 2, descending = false, after = page2.last().id)
		assertEquals(actual = page3, expected = page(e5))

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageBeforeAndAfterFormRange() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertEquals(
			actual = scope.loadEvents(limit = 10, after = e1.id, before = e5.id),
			expected = page(e4, e3, e2),
		)
		assertFailsWith<IllegalArgumentException> {
			scope.loadEvents(limit = 10, after = e3.id, before = e3.id)
		}

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageFiltersByAggregateType() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertEquals(
			actual = scope.loadEvents(limit = 10, changes = mapOf(BankAccountNumber::class to null)),
			expected = page(e5, e4, e3, e1),
		)
		assertEquals(
			actual = scope.loadEvents(limit = 10, changes = emptyMap()),
			expected = emptyList(),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageFiltersByAggregateTypeAndChangeTypeAcrossAggregates() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		// Both aggregates have a `created` change; only the listed pairs may match.
		assertEquals(
			actual = scope.loadEvents(
				limit = 10,
				changes = mapOf(
					BankAccountNumber::class to setOf(Deposited::class),
					CounterNumber::class to setOf(CounterChange.Created::class),
				),
			),
			expected = page(e4, e3, e2),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageMatchesNestedSealedLeafByExactClass() = runTest {
		val nestedEvent = RaptorAggregateEvent(
			aggregateId = LoadTestsNestedId("n"),
			change = LoadTestsNestedChange.Outer.Inner.Leaf,
			id = RaptorAggregateEventId(6),
			timestamp = Timestamp.fromEpochSeconds(6),
			version = 1,
		)
		val store = TestAggregateStore(events = seedEvents() + nestedEvent)
		val raptor = buildRaptor(store, includeNestedAggregate = true)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertEquals(
			actual = scope.loadEvents(
				limit = 10,
				changes = mapOf(LoadTestsNestedId::class to setOf(LoadTestsNestedChange.Outer.Inner.Leaf::class)),
			),
			expected = page(nestedEvent),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadAggregateEventsRejectsUnregisteredTypes() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store, includeIndividualAggregate = true)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertFailsWith<IllegalArgumentException> {
			scope.loadEvents(limit = 10, changes = mapOf(LoadTestsNestedId::class to null))
		}
		// `CounterChange.Created` is registered, but not for the bank account aggregate.
		assertFailsWith<IllegalArgumentException> {
			scope.loadEvents(limit = 10, changes = mapOf(BankAccountNumber::class to setOf(CounterChange.Created::class)))
		}
		assertFailsWith<IllegalStateException> {
			scope.loadEvents(limit = 10, changes = mapOf(LoadTestsIndividualId::class to null))
		}

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageSingleBoundInEitherDirection() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertEquals(actual = scope.loadEvents(limit = 2, after = e2.id), expected = page(e5, e4))
		assertEquals(actual = scope.loadEvents(limit = 2, descending = false, before = e4.id), expected = page(e1, e2))

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageEmptyChangeSetMatchesNothing() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertEquals(
			actual = scope.loadEvents(limit = 10, changes = mapOf(BankAccountNumber::class to emptySet())),
			expected = emptyList(),
		)

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageRejectsChangeDefinitionOfAnotherAggregate() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)

		val bankDefinition = definitionFor(raptor, bankAccountId1)
		val counterDefinition = definitionFor(raptor, counterId)
		val counterCreated = counterDefinition.changeDefinitions.first { it.changeClass == CounterChange.Created::class }

		assertFailsWith<IllegalArgumentException> {
			store.loadPage(limit = 10, changes = mapOf(bankDefinition to setOf(counterCreated)))
		}

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageLimitBounds() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		assertFailsWith<IllegalArgumentException> { scope.loadEvents(limit = 0) }
		assertFailsWith<IllegalArgumentException> { scope.loadEvents(limit = RaptorAggregateLoader.MAX_PAGE_SIZE + 1) }
		assertEquals(actual = scope.loadEvents(limit = RaptorAggregateLoader.MAX_PAGE_SIZE), expected = page(e5, e4, e3, e2, e1))
		assertEquals(actual = scope.loadEvents(limit = 1), expected = page(e5))

		raptor.lifecycle.stop()
	}


	@Test
	fun testLoadPageOlderPagesAreStableWhileEventsAreAddedAtHead() = runTest {
		val store = TestAggregateStore(events = seedEvents())
		val raptor = buildRaptor(store)
		raptor.lifecycle.startIn(this)
		val scope = raptor.context.asScope()

		val page1 = scope.loadEvents(limit = 2)

		val e6 = RaptorAggregateEvent(
			aggregateId = counterId,
			change = CounterChange.Incremented,
			id = RaptorAggregateEventId(6),
			timestamp = Timestamp.fromEpochSeconds(6),
			version = 2,
		)
		store.add(listOf(e6))

		assertEquals(actual = scope.loadEvents(limit = 2, before = page1.last().id), expected = page(e3, e2))
		assertEquals(actual = scope.loadEvents(limit = 2), expected = page(e6, e5))

		raptor.lifecycle.stop()
	}
}
