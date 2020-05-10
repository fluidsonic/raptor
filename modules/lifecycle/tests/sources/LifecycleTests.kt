package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.RaptorLifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*


@OptIn(ExperimentalCoroutinesApi::class)
class LifecycleTests {

	@Test
	fun testLifecycle() = runBlockingTest {
		val raptor = raptor {
			install(RaptorLifecycleFeature)
			install(StartableFeature) {
				delayInMilliseconds = 100
			}
		}

		val lifecycle = raptor.lifecycle
		val startable = raptor[StartableRaptorPropertyKey]!!

		repeat(10) {
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)

			launch {
				lifecycle.start()
			}

			assertEquals(expected = State.starting, actual = lifecycle.state)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.starting, actual = lifecycle.state)

			assertFails {
				lifecycle.start()
			}
			assertEquals(expected = State.starting, actual = lifecycle.state)

			advanceTimeBy(200)
			assertEquals(expected = State.started, actual = lifecycle.state)
			assertTrue(startable.isStarted)

			assertFails {
				lifecycle.start()
			}
			assertEquals(expected = State.started, actual = lifecycle.state)
			assertTrue(startable.isStarted)

			launch {
				lifecycle.stop()
			}
			assertEquals(expected = State.stopping, actual = lifecycle.state)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.stopping, actual = lifecycle.state)

			assertFails {
				lifecycle.start()
			}
			assertEquals(expected = State.stopping, actual = lifecycle.state)

			advanceTimeBy(200)
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)
		}
	}


	@Test
	fun testLifecycleWaitsForActions() = runBlockingTest {
		val raptor = raptor {
			install(RaptorLifecycleFeature)
			install(StartableFeature) {
				delayInMilliseconds = 1_000L
			}
		}

		val lifecycle = raptor.lifecycle

		assertEquals(expected = State.stopped, actual = lifecycle.state)

		launch {
			lifecycle.start()
		}

		advanceTimeBy(600)
		assertEquals(expected = State.starting, actual = lifecycle.state)

		advanceTimeBy(600)
		assertEquals(expected = State.started, actual = lifecycle.state)

		launch {
			lifecycle.stop()
		}

		advanceTimeBy(600)
		assertEquals(expected = State.stopping, actual = lifecycle.state)

		advanceTimeBy(600)
		assertEquals(expected = State.stopped, actual = lifecycle.state)
	}


	@Test
	fun testLifecycleWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorLifecycleFeature for enabling lifecycle functionality.",
			actual = assertFails {
				raptor.lifecycle
			}.message
		)
	}
}
