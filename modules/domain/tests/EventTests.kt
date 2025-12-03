import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.memory.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*


class EventTests {

	@Test
	fun testEventStreams() = runTest {
		val timestamp = Timestamp.fromEpochSeconds(1)
		val bankAccountNumber = BankAccountNumber("1")
		val carNumber = CarNumber("1234")

		val clock = ManualClock()
		clock.set(timestamp)

		val store = TestAggregateStore()

		val events: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf()
		val projectionEvents: MutableList<RaptorAggregateProjectionEvent<*, *, *>> = mutableListOf()

		fun handleBankAccountEvent(event: RaptorAggregateEvent<BankAccountNumber, BankAccountChange>) {
			events += event
		}

		fun handleBankAccountProjectionEvent(event: RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange>) {
			projectionEvents += event
		}

		fun handleCarEvent(event: RaptorAggregateEvent<CarNumber, CarChange>) {
			events += event
		}

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorEventPlugin) // FIXME Avoid?
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(clock)
				provide<Logger> { LoggerFactory.getLogger("test") }
			}

			domain.aggregates {
				individualStoreFactory(RaptorIndividualAggregateStoreFactory.memory())
				store(store)

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

				new(::CarAggregate, "car", individual = true) {
					command<CarCommand.Build>()
					command<CarCommand.Crash>()
					command<CarCommand.Deliver>()
					command<CarCommand.Drive>()

					change<CarChange.Built>("built")
					change<CarChange.Crashed>("crashed")
					change<CarChange.Delivered>("delivered")
					change<CarChange.Driven>("driven")
				}
			}

			lifecycle.onStart("event handlers") {
				aggregateEventSource.subscribeIn(this, ::handleBankAccountEvent)
				aggregateEventSource.subscribeIn(this, ::handleCarEvent)
				aggregateProjectionEventSource.subscribeIn(this, ::handleBankAccountProjectionEvent)
			}
		}

		raptor.lifecycle.startIn(this)

		with(raptor.context.asScope()) {
			execute(bankAccountNumber, BankAccountCommand.Create(owner = "owner"))

			// FIXME write nice API
			val carManager: RaptorIndividualAggregateManager<CarNumber, CarChange> = di.get()

			assertEquals(
				actual = carManager.fetch(carNumber),
				expected = emptyList(),
			)

			assertEquals(
				actual = carManager.commit(
					id = carNumber, expectedVersion = 0, changes = listOf(
						CarChange.Built,
						CarChange.Delivered,
						CarChange.Driven(10),
						CarChange.Crashed,
					), timestamp = Timestamp.fromEpochSeconds(1)
				),
				expected = listOf(
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Built,
						id = RaptorAggregateEventId(1),
						timestamp = timestamp,
						version = 1,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Delivered,
						id = RaptorAggregateEventId(2),
						timestamp = timestamp,
						version = 2,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Driven(distance = 10),
						id = RaptorAggregateEventId(3),
						timestamp = timestamp,
						version = 3,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Crashed,
						id = RaptorAggregateEventId(4),
						timestamp = timestamp,
						version = 4,
						lastVersionInBatch = 4
					),
				)
			)
			assertEquals(
				actual = carManager.fetch(carNumber),
				expected = listOf(
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Built,
						id = RaptorAggregateEventId(1),
						timestamp = timestamp,
						version = 1,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Delivered,
						id = RaptorAggregateEventId(2),
						timestamp = timestamp,
						version = 2,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Driven(distance = 10),
						id = RaptorAggregateEventId(3),
						timestamp = timestamp,
						version = 3,
						lastVersionInBatch = 4
					),
					RaptorAggregateEvent(
						aggregateId = carNumber,
						change = CarChange.Crashed,
						id = RaptorAggregateEventId(4),
						timestamp = timestamp,
						version = 4,
						lastVersionInBatch = 4
					),
				),
			)
		}

		raptor.lifecycle.stop()

		@Suppress("RemoveExplicitTypeArguments")
		assertEquals<List<RaptorAggregateEvent<*, *>>>(
			actual = events,
			expected = listOf(
				RaptorAggregateEvent(
					aggregateId = bankAccountNumber,
					change = BankAccountChange.Created(owner = "owner"),
					id = RaptorAggregateEventId(1),
					timestamp = Timestamp.fromEpochSeconds(1),
					version = 1,
				),
				RaptorAggregateEvent(
					aggregateId = carNumber,
					change = CarChange.Built,
					id = RaptorAggregateEventId(1),
					timestamp = timestamp,
					version = 1,
					lastVersionInBatch = 4
				),
				RaptorAggregateEvent(
					aggregateId = carNumber,
					change = CarChange.Delivered,
					id = RaptorAggregateEventId(2),
					timestamp = timestamp,
					version = 2,
					lastVersionInBatch = 4
				),
				RaptorAggregateEvent(
					aggregateId = carNumber,
					change = CarChange.Driven(distance = 10),
					id = RaptorAggregateEventId(3),
					timestamp = timestamp,
					version = 3,
					lastVersionInBatch = 4
				),
				RaptorAggregateEvent(
					aggregateId = carNumber,
					change = CarChange.Crashed,
					id = RaptorAggregateEventId(4),
					timestamp = timestamp,
					version = 4,
					lastVersionInBatch = 4
				),
			),
		)

		@Suppress("RemoveExplicitTypeArguments")
		assertEquals<List<RaptorAggregateProjectionEvent<*, *, *>>>(
			actual = projectionEvents,
			expected = listOf(
				RaptorAggregateProjectionEvent(
					change = BankAccountChange.Created(owner = "owner"),
					id = RaptorAggregateEventId(1),
					previousProjection = null,
					projection = BankAccount(amount = 0, id = bankAccountNumber, label = null, owner = "owner"),
					timestamp = Timestamp.fromEpochSeconds(1),
					version = 1,
				),
			),
		)
	}


	@Test
	fun testRequiresPositiveVersionNumber() {
		assertEquals(
			actual = assertFails {
				RaptorAggregateEvent(
					aggregateId = BankAccountNumber("1"),
					change = BankAccountChange.Created(owner = "owner"),
					id = RaptorAggregateEventId(1),
					timestamp = Timestamp.fromEpochSeconds(0),
					version = 0,
				)
			}.message,
			expected = "'version' must be positive: 0"
		)
	}
}
