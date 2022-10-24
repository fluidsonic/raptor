import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.raptor.transactions.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*


@OptIn(ExperimentalCoroutinesApi::class)
class EventTests {

	@Test
	fun testEventStreams() = runTest {
		val id = BankAccountNumber("1")

		val clock = ManualClock()
		val eventFactory = TestAggregateEventFactory(clock = clock)
		val store = TestAggregateStore()

		val events: MutableList<RaptorAggregateEvent<BankAccountNumber, BankAccountChange>> = mutableListOf()
		val projectionEvents: MutableList<RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange>> = mutableListOf()

		fun handleEvent(event: RaptorAggregateEvent<BankAccountNumber, BankAccountChange>) {
			events += event
		}

		fun handleProjectionEvent(event: RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange>) {
			projectionEvents += event
		}

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

			lifecycle.onStart {
				val configuration = context.plugins.domain
				configuration.aggregates.eventStream.subscribeIn(this, ::handleEvent)
				configuration.aggregates.projectionEventStream.subscribeIn(this, ::handleProjectionEvent)
			}
		}

		raptor.lifecycle.startIn(this)

		raptor.transaction {
			clock.set(Timestamp.fromEpochSeconds(1))
			execute(id, Create(owner = "owner"))
			commit()
		}

		raptor.lifecycle.stop()

		assertEquals<List<RaptorAggregateEvent<BankAccountNumber, BankAccountChange>>>(
			actual = events,
			expected = listOf(RaptorAggregateEvent(
				aggregateId = id,
				change = Created(owner = "owner"),
				id = RaptorAggregateEventId("1"),
				isReplay = false,
				timestamp = Timestamp.fromEpochSeconds(1),
				version = 1,
			)),
		)
		assertEquals<List<RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange>>>(
			actual = projectionEvents,
			expected = listOf(RaptorAggregateProjectionEvent(
				change = Created(owner = "owner"),
				id = RaptorAggregateEventId("1"),
				isReplay = false,
				previousProjection = null,
				projection = BankAccount(amount = 0, id = id, label = null, owner = "owner"),
				timestamp = Timestamp.fromEpochSeconds(1),
				version = 1,
			)),
		)
	}


	@Test
	fun testRequiresPositiveVersionNumber() {
		assertEquals(
			actual = assertFails {
				RaptorAggregateEvent(
					aggregateId = BankAccountNumber("1"),
					change = Created(owner = "owner"),
					id = RaptorAggregateEventId("event 1"),
					isReplay = false,
					timestamp = Timestamp.fromEpochSeconds(0),
					version = 0,
				)
			}.message,
			expected = "'version' must be positive: 0"
		)
	}
}
