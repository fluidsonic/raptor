import BankAccountChange.*
import BankAccountCommand.*
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
		val store = TestAggregateStore(events = listOf(RaptorAggregateEvent(
			aggregateId = id,
			data = Created(owner = "owner"),
			id = RaptorAggregateEventId("1"),
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

					change<Created>("created")
					change<Deleted>("deleted")
					change<Deposited>("deposited")
					change<Labeled>("labeled")
					change<Withdrawn>("withdrawn")
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
			RaptorAggregateEvent(
				aggregateId = id,
				data = Deposited(amount = 100),
				id = RaptorAggregateEventId("1"),
				timestamp = Timestamp.fromEpochSeconds(10),
				version = 2,
			),
			RaptorAggregateEvent(
				aggregateId = id,
				data = Labeled("test"),
				id = RaptorAggregateEventId("2"),
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
			RaptorAggregateEvent(
				aggregateId = id,
				data = Withdrawn(amount = 100),
				id = RaptorAggregateEventId("3"),
				timestamp = Timestamp.fromEpochSeconds(30),
				version = 4,
			),
			RaptorAggregateEvent(
				aggregateId = id,
				data = Deleted,
				id = RaptorAggregateEventId("4"),
				timestamp = Timestamp.fromEpochSeconds(40),
				version = 5,
			),
		)))

		raptor.lifecycle.stop()

		// FIXME test emit
	}
}
