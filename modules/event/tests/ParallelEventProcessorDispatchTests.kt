import io.fluidsonic.raptor.event.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*


class ParallelEventProcessorDispatchTests {

	private object TestEvent : RaptorEvent


	private suspend fun hasDispatch() =
		isProcessingRaptorEvent()


	@Test
	fun testOnlySyncHandlersRunWithDispatchMarker() = runTest {
		val otherScope = this
		val processor = ParallelEventProcessor()
		val seen = mutableMapOf<String, Boolean>()

		val subscriptionScope = CoroutineScope(coroutineContext + Job())
		with(subscriptionScope) {
			processor.subscribe<TestEvent>(handler = {
				seen["sync"] = hasDispatch()
				coroutineScope { launch { seen["sync child"] = hasDispatch() } }
				otherScope.launch { seen["sync other scope"] = hasDispatch() }.join()
			})
			processor.subscribe<TestEvent>(async = true, handler = {
				seen["async"] = hasDispatch()
			})
		}

		processor.process(TestEvent)
		testScheduler.advanceUntilIdle()

		assertEquals(
			expected = mapOf(
				"async" to false,
				"sync" to true,
				"sync child" to true,
				"sync other scope" to false,
			),
			actual = seen.toSortedMap(),
		)

		subscriptionScope.cancel()
	}
}
