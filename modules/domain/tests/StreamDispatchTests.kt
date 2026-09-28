import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.helpers.*


class StreamDispatchTests {

	private val id = BankAccountNumber("1")


	private fun buildRaptor(
		store: TestAggregateStore = TestAggregateStore(),
		subscribers: suspend RaptorLifecycleStartScope.() -> Unit,
	) =
		raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(ManualClock().also { it.set(Timestamp.fromEpochSeconds(2)) })
				provide<org.slf4j.Logger>(NOPLogger.NOP_LOGGER)
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

			lifecycle.onStart("stream subscribers", action = subscribers)
		}


	private suspend fun hasDispatch() =
		currentCoroutineContext()[RaptorAggregateStreamDispatch] != null


	@Test
	fun testSubscribersRunWithDispatchMarker() = runTest {
		val otherScope = this
		val seen = mutableMapOf<String, MutableList<Boolean>>()
		val consumedFromChannel = mutableListOf<Boolean>()
		val directSeen = mutableSetOf<Boolean>()
		val channel = Channel<Unit>(Channel.UNLIMITED)

		fun record(key: String, value: Boolean) {
			seen.getOrPut(key) { mutableListOf() } += value
		}

		val consumer = launch {
			for (unit in channel)
				consumedFromChannel += hasDispatch()
		}

		val store = TestAggregateStore(events = listOf(RaptorAggregateEvent(
			aggregateId = id,
			change = Created(owner = "owner"),
			id = RaptorAggregateEventId(1),
			timestamp = Timestamp.fromEpochSeconds(1),
			version = 1,
		)))

		val raptor = buildRaptor(store) {
			aggregateStream.subscribeIn<BankAccountNumber, BankAccountChange>(this, collector = { _: RaptorAggregateEvent<BankAccountNumber, BankAccountChange> ->
				record("aggregate", hasDispatch())
				coroutineScope { launch { record("aggregate child", hasDispatch()) } }
				otherScope.launch { record("aggregate other scope", hasDispatch()) }.join()
				channel.send(Unit)
				flowOf(1)
					.onEach { record("aggregate buffer producer", hasDispatch()) }
					.buffer()
					.collect { record("aggregate buffer collector", hasDispatch()) }
			})
			aggregateStream.messages
				.onEach { directSeen += hasDispatch() }
				.launchIn(this)
			aggregateStream.subscribeIn<BankAccountNumber, BankAccountChange>(this, collector = { _: RaptorAggregateEventBatch<BankAccountNumber, BankAccountChange> ->
				record("aggregate batch", hasDispatch())
			})
			aggregateProjectionStream.subscribeIn<BankAccountNumber, BankAccountChange, BankAccount>(this, collector = { _: RaptorAggregateProjectionEvent<BankAccountNumber, BankAccount, BankAccountChange> ->
				record("projection", hasDispatch())
			})
			aggregateProjectionStream.subscribeIn<BankAccountNumber, BankAccountChange, BankAccount>(
				this,
				collector = { _: RaptorAggregateProjectionEventBatch<BankAccountNumber, BankAccount, BankAccountChange> ->
					record("projection batch", hasDispatch())
				},
			)
			aggregateProjectionStream.subscribeMessagesIn<BankAccountNumber, BankAccountChange, BankAccount>(this) { message ->
				val key = when (message) {
					is RaptorAggregateProjectionEventBatch -> "messages batch"
					RaptorAggregateProjectionStreamMessage.Loaded -> "messages loaded"
					else -> "messages other"
				}
				record(key, hasDispatch())
			}
		}

		raptor.lifecycle.startIn(this)

		with(raptor.context.asScope()) {
			execute(id, Deposit(amount = 10))
		}

		raptor.lifecycle.stop()
		channel.close()
		consumer.join()

		assertEquals(
			expected = mapOf(
				"aggregate" to listOf(true),
				"aggregate batch" to listOf(true),
				"aggregate buffer collector" to listOf(true),
				"aggregate buffer producer" to listOf(true),
				"aggregate child" to listOf(true),
				"aggregate other scope" to listOf(false),
				"messages batch" to listOf(true, true), // replayed batch + live batch
				"messages loaded" to listOf(true),
				"projection" to listOf(true),
				"projection batch" to listOf(true),
			),
			actual = seen.toSortedMap(),
		)
		assertEquals(expected = listOf(false), actual = consumedFromChannel)
		assertEquals(expected = setOf(false), actual = directSeen, "direct 'messages' collectors aren't marked")
	}


	// Streams are unbuffered: emitting a batch returns once every subscriber has taken it, but the next emission
	// (and thus the next commit, which emits while holding the commit lock) waits until the handler has finished.
	@Test
	fun testRunningHandlerHoldsUpNextCommit() = runTest {
		val release = CompletableDeferred<Unit>()
		var handledCount = 0

		val raptor = buildRaptor {
			aggregateStream.subscribeIn<BankAccountNumber, BankAccountChange>(this, collector = { _: RaptorAggregateEvent<BankAccountNumber, BankAccountChange> ->
				handledCount += 1
				release.await()
			})
		}

		raptor.lifecycle.startIn(this)

		with(raptor.context.asScope()) {
			execute(id, Create(owner = "owner"))
			testScheduler.advanceUntilIdle()
			assertEquals(expected = 1, actual = handledCount)

			val secondCommit = launch { execute(id, Deposit(amount = 10)) }
			testScheduler.advanceUntilIdle()
			assertFalse(secondCommit.isCompleted, "commit must wait for the running handler")

			release.complete(Unit)
			secondCommit.join()
			testScheduler.advanceUntilIdle()
			assertEquals(expected = 2, actual = handledCount)
		}

		raptor.lifecycle.stop()
	}
}
