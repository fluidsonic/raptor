import BankAccountCommand.*
import BankAccountEvent.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.raptor.transactions.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*


@OptIn(ExperimentalCoroutinesApi::class)
class ExecutionTests {

	@Test
	fun testExecution() = runTest {
		val clock = ManualClock()
		val id = BankAccountNumber("1")
		val eventFactory = TestAggregateEventFactory(clock = clock)
		val store = TestAggregateStore(events = listOf(RaptorEvent(
			aggregateId = id,
			data = Created(owner = "owner"),
			id = RaptorEventId("1"),
			timestamp = Timestamp.fromEpochSeconds(0),
			version = 1,
		)))

		val raptor = raptor {
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorTransactionPlugin)

			domain.aggregates {
				eventFactory(eventFactory)
				store(store)

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<Create>()
					command<Delete>()
					command<Deposit>()
					command<Label>()
					command<Withdraw>()

					event<Created>("created")
					event<Deleted>("deleted")
					event<Deposited>("deposited")
					event<Labeled>("labeled")
					event<Withdrawn>("withdrawn")
				}
			}
		}
		raptor.lifecycle.startIn(this)

		raptor.transaction {
			clock.set(Timestamp.fromEpochSeconds(10))
			execute(id, Deposit(amount = 100))

			clock.set(Timestamp.fromEpochSeconds(20))
			execute(id, Label("test"))
		}
		assertEquals(actual = store.takeBatches(), expected = listOf(listOf(
			RaptorEvent(
				aggregateId = id,
				data = Deposited(amount = 100),
				id = RaptorEventId("1"),
				timestamp = Timestamp.fromEpochSeconds(10),
				version = 2,
			),
			RaptorEvent(
				aggregateId = id,
				data = Labeled("test"),
				id = RaptorEventId("2"),
				timestamp = Timestamp.fromEpochSeconds(20),
				version = 3,
			),
		)))

		raptor.transaction {
			clock.set(Timestamp.fromEpochSeconds(30))
			execute(id, Withdraw(amount = 100))

			clock.set(Timestamp.fromEpochSeconds(40))
			execute(id, Delete)
		}
		assertEquals(actual = store.takeBatches(), expected = listOf(listOf(
			RaptorEvent(
				aggregateId = id,
				data = Withdrawn(amount = 100),
				id = RaptorEventId("3"),
				timestamp = Timestamp.fromEpochSeconds(30),
				version = 4,
			),
			RaptorEvent(
				aggregateId = id,
				data = Deleted,
				id = RaptorEventId("4"),
				timestamp = Timestamp.fromEpochSeconds(40),
				version = 5,
			),
		)))

		raptor.lifecycle.stop()

		// FIXME test emit
	}
}
