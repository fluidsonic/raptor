import io.fluidsonic.raptor.event.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*


class EventDispatchTests {

	private object TestEvent : RaptorEvent


	private suspend fun hasDispatch() =
		isProcessingRaptorEvent()


	@Test
	fun testSubscribersRunWithDispatchMarker() = runTest {
		val otherScope = this
		val processor = ParallelDispatchEventProcessor(onError = { _, _ -> })
		val seen = mutableMapOf<String, Boolean>()

		val subscriptionScope = CoroutineScope(coroutineContext + Job())
		processor.subscribeIn<TestEvent>(subscriptionScope) {
			seen["handler"] = hasDispatch()
			coroutineScope { launch { seen["handler child"] = hasDispatch() } }
			otherScope.launch { seen["handler other scope"] = hasDispatch() }.join()
			flowOf(1)
				.onEach { seen["buffer producer"] = hasDispatch() }
				.buffer()
				.collect { seen["buffer collector"] = hasDispatch() }
		}
		processor.asFlow()
			.onEach { seen["direct collector"] = hasDispatch() }
			.launchIn(subscriptionScope)
		testScheduler.advanceUntilIdle()

		processor.process(TestEvent)
		testScheduler.advanceUntilIdle()

		assertEquals(
			expected = mapOf(
				"buffer collector" to true,
				"buffer producer" to true,
				"direct collector" to false,
				"handler" to true,
				"handler child" to true,
				"handler other scope" to false,
			),
			actual = seen.toSortedMap(),
		)

		subscriptionScope.cancel()
	}
}
