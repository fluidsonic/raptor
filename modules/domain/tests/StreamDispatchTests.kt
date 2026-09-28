import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.memory.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.helpers.*


class StreamDispatchTests {

	private val id = BankAccountNumber("1")


	private suspend fun hasDispatch() =
		currentCoroutineContext()[RaptorAggregateStreamDispatch] != null


	@Test
	fun testOnlySyncSubscribersRunWithDispatchMarker() = runTest {
		val otherScope = this
		val seen = mutableMapOf<String, MutableList<Boolean>>()

		fun record(key: String, value: Boolean) {
			seen.getOrPut(key) { mutableListOf() } += value
		}

		val clock = ManualClock().also { it.set(Timestamp.fromEpochSeconds(2)) }

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorEventPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(clock)
				provide<org.slf4j.Logger>(NOPLogger.NOP_LOGGER)
			}

			domain.aggregates {
				individualStoreFactory(RaptorIndividualAggregateStoreFactory.memory())
				store(TestAggregateStore(events = listOf(RaptorAggregateEvent(
					aggregateId = id,
					change = BankAccountChange.Created(owner = "owner"),
					id = RaptorAggregateEventId(1),
					timestamp = Timestamp.fromEpochSeconds(1),
					version = 1,
				))))

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
			}

			lifecycle.onStart("event handlers") {
				aggregateEventSource.subscribeIn(this, replay = true, handler = { _: RaptorAggregateEvent<BankAccountNumber, BankAccountChange> ->
					record("sync", hasDispatch())
					coroutineScope { launch { record("sync child", hasDispatch()) } }
					otherScope.launch { record("sync other scope", hasDispatch()) }.join()
				})
				aggregateEventSource.subscribeIn(this, async = true, handler = { _: RaptorAggregateEvent<BankAccountNumber, BankAccountChange> ->
					record("async", hasDispatch())
				})
				aggregateProjectionEventSource.subscribeIn(
					this,
					handler = { _: RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange> ->
						record("projection sync", hasDispatch())
					},
				)
				aggregateProjectionEventSource.subscribeIn(
					this,
					async = true,
					handler = { _: RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange> ->
						record("projection async", hasDispatch())
					},
				)
				val eventSource = aggregateEventSource
				context(this) {
					eventSource.subscribe(handler = { _: RaptorAggregateReplayCompletedEvent ->
						record("replay completed", hasDispatch())
					})
				}
			}
		}

		raptor.lifecycle.startIn(this)

		with(raptor.context.asScope()) {
			execute(id, BankAccountCommand.Deposit(amount = 10))
		}

		testScheduler.advanceUntilIdle()
		raptor.lifecycle.stop()

		assertEquals(
			expected = mapOf(
				"async" to listOf(false),
				"projection async" to listOf(false),
				"projection sync" to listOf(true),
				"replay completed" to listOf(true),
				"sync" to listOf(true, true), // replayed + live
				"sync child" to listOf(true, true),
				"sync other scope" to listOf(false, false),
			),
			actual = seen.toSortedMap(),
		)
	}
}
