package io.fluidsonic.raptor.service2

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*


class RaptorServiceWorkerTests {

	@Test
	fun `schedule executes block in scope`() = runTest {
		val worker = DefaultRaptorServiceWorker(this)
		var executed = false

		val job = worker.schedule { executed = true }
		job.join()

		assertTrue(executed)
	}


	@Test
	fun `scheduleAsync returns result`() = runTest {
		val worker = DefaultRaptorServiceWorker(this)

		val deferred = worker.scheduleAsync { 42 }

		assertEquals(actual = deferred.await(), expected = 42)
	}


	@Test
	fun `replaceScope updates the worker scope`() = runTest {
		val initialScope = CoroutineScope(Job())
		val worker = DefaultRaptorServiceWorker(initialScope)

		// Cancel initial scope
		initialScope.cancel()

		// Replace with test scope
		worker.replaceScope(this)

		// Should work with new scope
		var executed = false
		worker.schedule { executed = true }.join()

		assertTrue(executed)
	}


	@Test
	fun `asContextElement sets and restores worker in coroutine context`() = runTest {
		assertNull(CurrentServiceWorker.current)

		val worker = object : RaptorServiceWorker {
			override fun schedule(block: suspend () -> Unit): Job = Job()
			override fun <T> scheduleAsync(block: suspend () -> T): Deferred<T> = CompletableDeferred()
		}

		withContext(CurrentServiceWorker.asContextElement(worker)) {
			assertSame(actual = CurrentServiceWorker.current, expected = worker)
		}

		assertNull(CurrentServiceWorker.current)
	}


	@Test
	fun `asContextElement restores previous worker after nested context`() = runTest {
		val outerWorker = object : RaptorServiceWorker {
			override fun schedule(block: suspend () -> Unit): Job = Job()
			override fun <T> scheduleAsync(block: suspend () -> T): Deferred<T> = CompletableDeferred()
		}
		val innerWorker = object : RaptorServiceWorker {
			override fun schedule(block: suspend () -> Unit): Job = Job()
			override fun <T> scheduleAsync(block: suspend () -> T): Deferred<T> = CompletableDeferred()
		}

		withContext(CurrentServiceWorker.asContextElement(outerWorker)) {
			assertSame(actual = CurrentServiceWorker.current, expected = outerWorker)

			withContext(CurrentServiceWorker.asContextElement(innerWorker)) {
				assertSame(actual = CurrentServiceWorker.current, expected = innerWorker)
			}

			assertSame(actual = CurrentServiceWorker.current, expected = outerWorker)
		}

		assertNull(CurrentServiceWorker.current)
	}


	@Test
	fun `replaceScope throws after work has been scheduled`() = runTest {
		val worker = DefaultRaptorServiceWorker(this)
		worker.schedule { }

		assertFailsWith<IllegalStateException> {
			worker.replaceScope(this)
		}
	}


	@Test
	fun `replaceScope throws after scheduleAsync has been called`() = runTest {
		val worker = DefaultRaptorServiceWorker(this)
		worker.scheduleAsync { 1 }

		assertFailsWith<IllegalStateException> {
			worker.replaceScope(this)
		}
	}


	@Test
	fun `schedule uses UNDISPATCHED start`() = runTest {
		val worker = DefaultRaptorServiceWorker(this)
		var executionOrder = mutableListOf<Int>()

		worker.schedule {
			executionOrder.add(1)
			yield()
			executionOrder.add(3)
		}
		executionOrder.add(2)

		// With UNDISPATCHED, 1 executes immediately before returning
		// Then 2 is added, then 3 after yield
		runCurrent()

		assertEquals(
			actual = executionOrder,
			expected = listOf(1, 2, 3),
		)
	}
}
