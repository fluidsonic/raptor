package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.RaptorLifecycle.*
import kotlin.test.*
import kotlin.test.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*


@OptIn(ExperimentalCoroutinesApi::class)
class LifecycleTests {

	// TODO Needs to intentionally delay starts & stops for proper teseting.
	@Disabled
	@Test
	fun testLifecycle() = runTest {
		val raptor = raptor {
			install(RaptorLifecyclePlugin)
			install(StartablePlugin)

			startable.delayInMilliseconds = 100
		}

		val lifecycle = raptor.lifecycle
		val startable = raptor[Startable.propertyKey]!!

		repeat(10) {
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.stopped, actual = lifecycle.state)
			assertFalse(startable.isStarted)

			launch {
				lifecycle.startIn(this)
			}
			assertEquals(expected = State.starting, actual = lifecycle.state)

			assertFails {
				lifecycle.stop()
			}
			assertEquals(expected = State.starting, actual = lifecycle.state)

			assertFails {
				lifecycle.startIn(this)
			}
			assertEquals(expected = State.starting, actual = lifecycle.state)

			advanceTimeBy(200)
			assertEquals(expected = State.started, actual = lifecycle.state)
			assertTrue(startable.isStarted)

			assertFails {
				lifecycle.startIn(this)
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
				lifecycle.startIn(this)
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
	fun testLifecycleWaitsForActions() = runTest {
		val raptor = raptor {
			install(RaptorLifecyclePlugin)
			install(StartablePlugin)

			startable.delayInMilliseconds = 1_000L
		}

		val lifecycle = raptor.lifecycle

		assertEquals(expected = State.stopped, actual = lifecycle.state)

		launch {
			lifecycle.startIn(this)
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
			expected = "Plugin io.fluidsonic.raptor.RaptorLifecyclePlugin is not installed.",
			actual = assertFails {
				raptor.lifecycle
			}.message
		)
	}
}
