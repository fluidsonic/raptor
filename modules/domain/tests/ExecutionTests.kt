import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*


class ExecutionTests {

	@Test
	fun testExecution() = runTest {
		val id = BankAccountNumber("1")

		val clock = ManualClock()
		val store = TestAggregateStore(
			events = listOf(
				RaptorAggregateEvent(
					aggregateId = id,
					change = Created(owner = "owner"),
					id = RaptorAggregateEventId(1),
					timestamp = Timestamp.fromEpochSeconds(0),
					version = 1,
				)
			)
		)

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorEventPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(clock)
				provide<Logger> { LoggerFactory.getLogger("test") }
			}

			domain.aggregates {
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

		with(raptor.context.asScope()) {
			execution {
				clock.set(Timestamp.fromEpochSeconds(10))
				execute(id, Deposit(amount = 100))

				clock.set(Timestamp.fromEpochSeconds(20))
				execute(id, Label("test"))
			}
		}
		assertEquals(
			actual = store.takeBatches(), expected = listOf(
				listOf(
					RaptorAggregateEvent(
						aggregateId = id,
						change = Deposited(amount = 100),
						id = RaptorAggregateEventId(2),
						lastVersionInBatch = 3,
						timestamp = Timestamp.fromEpochSeconds(20),
						version = 2,
					),
					RaptorAggregateEvent(
						aggregateId = id,
						change = Labeled("test"),
						id = RaptorAggregateEventId(3),
						timestamp = Timestamp.fromEpochSeconds(20),
						version = 3,
					),
				)
			)
		)

		with(raptor.context.asScope()) {
			execution {
				clock.set(Timestamp.fromEpochSeconds(30))
				execute(id, Withdraw(amount = 100))

				clock.set(Timestamp.fromEpochSeconds(40))
				execute(id, Delete)
			}
		}
		assertEquals(
			actual = store.takeBatches(), expected = listOf(
				listOf(
					RaptorAggregateEvent(
						aggregateId = id,
						change = Withdrawn(amount = 100),
						id = RaptorAggregateEventId(4),
						lastVersionInBatch = 5,
						timestamp = Timestamp.fromEpochSeconds(40),
						version = 4,
					),
					RaptorAggregateEvent(
						aggregateId = id,
						change = Deleted,
						id = RaptorAggregateEventId(5),
						timestamp = Timestamp.fromEpochSeconds(40),
						version = 5,
					),
				)
			)
		)

		raptor.lifecycle.stop()

		// FIXME test emit
	}
}
