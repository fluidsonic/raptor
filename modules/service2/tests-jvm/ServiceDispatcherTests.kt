package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.service2.ServiceDispatcher.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*


class ServiceDispatcherTests {

	/**
	 * Starts the dispatcher with a [CoroutineExceptionHandler] that silently absorbs task exceptions.
	 * Use in tests that intentionally throw from tasks, so `runTest` doesn't capture them as failures.
	 */
	context(scope: CoroutineScope)
	private fun ServiceDispatcher.startIgnoringTaskExceptions() {
		val wrappedScope = CoroutineScope(scope.coroutineContext + CoroutineExceptionHandler { _, _ -> })
		with(wrappedScope) { start() }
	}


	// region lifecycle

	@Test
	fun `launchPrimaryTask before start throws`() {
		val dispatcher = ServiceDispatcher(serviceName = "test")

		assertFailsWith<IllegalStateException> {
			dispatcher.launchPrimaryTask("task") {}
		}
	}


	@Test
	fun `shutdown completes running tasks within timeout`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val completed = CompletableDeferred<Boolean>()

		dispatcher.launchPrimaryTask("task") {
			yield()
			completed.complete(true)
		}

		dispatcher.shutdown(timeout = 5.seconds)

		assertTrue(completed.isCompleted)
		assertTrue(completed.await())
	}


	@Test
	fun `shutdown with timeout cancels remaining tasks`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val started = CompletableDeferred<Unit>()
		var taskCompleted = false

		dispatcher.launchPrimaryTask("stuck") {
			started.complete(Unit)
			delay(Duration.INFINITE)
			taskCompleted = true
		}

		started.await()
		dispatcher.shutdown(timeout = 50.milliseconds)

		assertFalse(taskCompleted)
	}


	@Test
	fun `shutdown on idle dispatcher completes immediately`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `start twice throws`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		assertFailsWith<IllegalStateException> {
			dispatcher.start()
		}

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `toString includes service name`() {
		val dispatcher = ServiceDispatcher(serviceName = "my-service")

		assertEquals(
			actual = dispatcher.toString(),
			expected = "RaptorService[my-service]/coroutine-dispatcher",
		)
	}

	// endregion


	// region primary task scheduling

	@Test
	fun `single primary task runs to completion`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val result = CompletableDeferred<Int>()

		dispatcher.launchPrimaryTask("task") {
			result.complete(42)
		}

		assertEquals(actual = result.await(), expected = 42)
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `multiple primary tasks serialize in FIFO order`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<Int>()
		val gate = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		for (i in 1 .. 5) {
			dispatcher.launchPrimaryTask("task-$i") {
				if (i == 1) gate.await()

				order.add(i)

				if (i == 5) allDone.complete(Unit)
			}
		}

		// Ensure all tasks are queued before the first one starts executing.
		gate.complete(Unit)
		allDone.await()

		assertEquals(actual = order.toList(), expected = listOf(1, 2, 3, 4, 5))
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `primary task continuations are prioritized over queued primaries`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val task1Started = CompletableDeferred<Unit>()
		val task2Queued = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		dispatcher.launchPrimaryTask("task-1") {
			order.add("task-1-before-yield")
			task1Started.complete(Unit)
			task2Queued.await()
			yield()
			order.add("task-1-after-yield")
		}

		task1Started.await()

		dispatcher.launchPrimaryTask("task-2") {
			order.add("task-2")
			allDone.complete(Unit)
		}

		task2Queued.complete(Unit)
		allDone.await()

		// task-1's continuation must complete before task-2 starts.
		assertEquals(
			actual = order.toList(),
			expected = listOf("task-1-before-yield", "task-1-after-yield", "task-2"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `primary task failure does not block subsequent primary tasks`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.startIgnoringTaskExceptions()

		val secondCompleted = CompletableDeferred<Boolean>()

		dispatcher.launchPrimaryTask("failing") {
			throw RuntimeException("intentional")
		}

		dispatcher.launchPrimaryTask("succeeding") {
			secondCompleted.complete(true)
		}

		assertTrue(secondCompleted.await())
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `primary task runs across suspension points without interruption`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val longRunningStarted = CompletableDeferred<Unit>()
		val followerQueued = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		dispatcher.launchPrimaryTask("long-running") {
			order.add("step-1")
			longRunningStarted.complete(Unit)
			followerQueued.await()
			yield()
			order.add("step-2")
			yield()
			order.add("step-3")
		}

		longRunningStarted.await()

		dispatcher.launchPrimaryTask("follower") {
			order.add("follower")
			allDone.complete(Unit)
		}

		followerQueued.complete(Unit)
		allDone.await()

		assertEquals(
			actual = order.toList(),
			expected = listOf("step-1", "step-2", "step-3", "follower"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}

	// endregion


	// region background task scheduling

	@Test
	fun `single background task runs when no primary is active`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val result = CompletableDeferred<Int>()

		dispatcher.launchBackgroundTask("bg") {
			result.complete(99)
		}

		assertEquals(actual = result.await(), expected = 99)
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background task is blocked while primary is active`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val primaryStarted = CompletableDeferred<Unit>()
		val backgroundQueued = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		dispatcher.launchPrimaryTask("primary") {
			order.add("primary-start")
			primaryStarted.complete(Unit)
			backgroundQueued.await()
			yield()
			order.add("primary-end")
		}

		primaryStarted.await()

		dispatcher.launchBackgroundTask("bg") {
			order.add("background")
			allDone.complete(Unit)
		}

		backgroundQueued.complete(Unit)
		allDone.await()

		// Background must wait until primary fully completes.
		assertEquals(
			actual = order.toList(),
			expected = listOf("primary-start", "primary-end", "background"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background task launched from primary gets parent and root IDs`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val taskInfo = CompletableDeferred<Pair<Long, Long>>()

		dispatcher.launchPrimaryTask("parent") {
			dispatcher.launchBackgroundTask("child") {
				val task = currentCoroutineContext()[ServiceDispatcher.Task]
				checkNotNull(task)
				taskInfo.complete(task.parentId to task.rootId)
			}
		}

		val (parentId, rootId) = taskInfo.await()
		assertTrue(parentId > 0L)
		assertEquals(actual = rootId, expected = parentId)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background task launched outside primary has zero parentId`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val taskInfo = CompletableDeferred<Pair<Long, Long>>()

		dispatcher.launchBackgroundTask("standalone") {
			val task = currentCoroutineContext()[ServiceDispatcher.Task]
			checkNotNull(task)
			taskInfo.complete(task.parentId to task.rootId)
		}

		val (parentId, rootId) = taskInfo.await()
		assertEquals(actual = parentId, expected = 0L)
		assertTrue(rootId > 0L)

		dispatcher.shutdown(timeout = 1.seconds)
	}

	// endregion


	// region priority gating

	@Test
	fun `background tasks run in FIFO order when no primary is active`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<Int>()
		val gate = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		for (i in 1 .. 5) {
			dispatcher.launchBackgroundTask("bg-$i") {
				if (i == 1) gate.await()

				order.add(i)

				if (i == 5) allDone.complete(Unit)
			}
		}

		gate.complete(Unit)
		allDone.await()

		assertEquals(actual = order.toList(), expected = listOf(1, 2, 3, 4, 5))
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background yields to incoming primary task`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val backgroundStarted = CompletableDeferred<Unit>()
		val primaryQueued = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		dispatcher.launchBackgroundTask("bg") {
			order.add("bg-before-yield")
			backgroundStarted.complete(Unit)
			primaryQueued.await()
			yield()
			order.add("bg-after-yield")
			allDone.complete(Unit)
		}

		backgroundStarted.await()

		dispatcher.launchPrimaryTask("primary") {
			order.add("primary")
		}

		primaryQueued.complete(Unit)
		allDone.await()

		// Primary should preempt background's continuation.
		assertEquals(
			actual = order.toList(),
			expected = listOf("bg-before-yield", "primary", "bg-after-yield"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `mixed workload of primaries and backgrounds maintains ordering`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val gate = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		// Enqueue: P1, P2, B1, B2
		dispatcher.launchPrimaryTask("p1") {
			gate.await()
			order.add("P1")
		}
		dispatcher.launchPrimaryTask("p2") { order.add("P2") }
		dispatcher.launchBackgroundTask("b1") { order.add("B1") }
		dispatcher.launchBackgroundTask("b2") {
			order.add("B2")
			allDone.complete(Unit)
		}

		// Open the gate so all four can execute in order.
		gate.complete(Unit)
		allDone.await()

		// Primaries first (FIFO), then backgrounds (FIFO).
		assertEquals(
			actual = order.toList(),
			expected = listOf("P1", "P2", "B1", "B2"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `primary enqueued during background causes background to wait`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val backgroundStarted = CompletableDeferred<Unit>()
		val primaryQueued = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		dispatcher.launchBackgroundTask("bg") {
			order.add("bg-start")
			backgroundStarted.complete(Unit)
			primaryQueued.await()
			yield()
			order.add("bg-end")
			allDone.complete(Unit)
		}

		backgroundStarted.await()

		// Enqueue primary while background is active — primary takes priority.
		dispatcher.launchPrimaryTask("primary") {
			order.add("primary")
		}

		primaryQueued.complete(Unit)
		allDone.await()

		assertEquals(
			actual = order.toList(),
			expected = listOf("bg-start", "primary", "bg-end"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `rapid primary-background-primary correctly gates`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val order = ConcurrentLinkedQueue<String>()
		val gate = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		// P1, B1, P2 — expected: P1, P2, B1
		dispatcher.launchPrimaryTask("p1") {
			gate.await()
			order.add("P1")
		}
		dispatcher.launchBackgroundTask("b1") {
			order.add("B1")
			allDone.complete(Unit)
		}
		dispatcher.launchPrimaryTask("p2") { order.add("P2") }

		gate.complete(Unit)
		allDone.await()

		assertEquals(
			actual = order.toList(),
			expected = listOf("P1", "P2", "B1"),
		)

		dispatcher.shutdown(timeout = 1.seconds)
	}

	// endregion


	// region task context

	@Test
	fun `Task is in coroutine context during primary task`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val taskInfo = CompletableDeferred<ServiceDispatcher.Task>()

		dispatcher.launchPrimaryTask("my-task") {
			val task = currentCoroutineContext()[ServiceDispatcher.Task]
			checkNotNull(task)
			taskInfo.complete(task)
		}

		val task = taskInfo.await()

		assertEquals(actual = task.label, expected = "my-task")
		assertEquals(actual = task.type, expected = TaskType.primary)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `Task is in coroutine context during background task`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val taskInfo = CompletableDeferred<ServiceDispatcher.Task>()

		dispatcher.launchBackgroundTask("my-bg-task") {
			val task = currentCoroutineContext()[ServiceDispatcher.Task]
			checkNotNull(task)
			taskInfo.complete(task)
		}

		val task = taskInfo.await()

		assertEquals(actual = task.label, expected = "my-bg-task")
		assertEquals(actual = task.type, expected = TaskType.background)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `task IDs increment monotonically`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val ids = ConcurrentLinkedQueue<Long>()
		val gate = CompletableDeferred<Unit>()
		val allDone = CompletableDeferred<Unit>()

		for (i in 1 .. 5) {
			dispatcher.launchPrimaryTask("task-$i") {
				if (i == 1) gate.await()

				val task = currentCoroutineContext()[ServiceDispatcher.Task]
				checkNotNull(task)
				ids.add(task.id)

				if (i == 5) allDone.complete(Unit)
			}
		}

		gate.complete(Unit)
		allDone.await()

		val idList = ids.toList()
		assertEquals(actual = idList.size, expected = 5)

		for (i in 1 until idList.size) {
			assertTrue(idList[i] > idList[i - 1], "IDs must be strictly increasing: $idList")
		}

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `dispatch without Task in context throws`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		assertFailsWith<IllegalStateException> {
			dispatcher.dispatch(coroutineContext) {}
		}

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `primary task detects dispatcher switch`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val error = CompletableDeferred<Throwable?>()

		dispatcher.launchPrimaryTask("task") {
			try {
				withContext(Dispatchers.Default) {
					// This should trigger the check in updateThreadContext.
				}
				error.complete(null)
			}
			catch (exception: IllegalStateException) {
				error.complete(exception)
			}
		}

		val result = error.await()
		assertNotNull(result)
		assertTrue(result.message?.contains("must not run on a different dispatcher") == true)

		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background task does not throw on dispatcher switch`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val completed = CompletableDeferred<Boolean>()

		dispatcher.launchBackgroundTask("bg") {
			withContext(Dispatchers.IO) {
				// No exception expected.
			}
			completed.complete(true)
		}

		assertTrue(completed.await())
		dispatcher.shutdown(timeout = 1.seconds)
	}

	// endregion


	// region error and edge cases

	@Test
	fun `primary task exception is isolated`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.startIgnoringTaskExceptions()

		val secondResult = CompletableDeferred<String>()

		dispatcher.launchPrimaryTask("failing") {
			throw RuntimeException("fail")
		}

		dispatcher.launchPrimaryTask("succeeding") {
			secondResult.complete("ok")
		}

		assertEquals(actual = secondResult.await(), expected = "ok")
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `background task exception is isolated`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.startIgnoringTaskExceptions()

		val secondResult = CompletableDeferred<String>()

		dispatcher.launchBackgroundTask("failing") {
			throw RuntimeException("fail")
		}

		dispatcher.launchBackgroundTask("succeeding") {
			secondResult.complete("ok")
		}

		assertEquals(actual = secondResult.await(), expected = "ok")
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `multiple task failures do not cascade`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.startIgnoringTaskExceptions()

		val successResult = CompletableDeferred<String>()

		dispatcher.launchPrimaryTask("fail-1") { throw RuntimeException("fail-1") }
		dispatcher.launchPrimaryTask("fail-2") { throw RuntimeException("fail-2") }
		dispatcher.launchBackgroundTask("fail-3") { throw RuntimeException("fail-3") }
		dispatcher.launchPrimaryTask("success") {
			successResult.complete("done")
		}

		assertEquals(actual = successResult.await(), expected = "done")
		dispatcher.shutdown(timeout = 1.seconds)
	}


	@Test
	fun `shutdown after task failures completes cleanly`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.startIgnoringTaskExceptions()

		val allFailed = CompletableDeferred<Unit>()

		dispatcher.launchPrimaryTask("fail-1") { throw RuntimeException("fail-1") }
		dispatcher.launchPrimaryTask("fail-2") {
			throw RuntimeException("fail-2")
		}
		dispatcher.launchPrimaryTask("signal") {
			allFailed.complete(Unit)
		}

		allFailed.await()
		dispatcher.shutdown(timeout = 1.seconds)
	}

	// endregion


	// region race conditions

	@Test
	fun `concurrent primary tasks from multiple threads all execute`() = runTest {
		repeat(100) {
			val dispatcher = ServiceDispatcher(serviceName = "test")
			dispatcher.start()

			val taskCount = 20
			val counter = AtomicInteger(0)
			val latch = CountDownLatch(taskCount)

			val threads = (1 .. taskCount).map { i ->
				Thread {
					dispatcher.launchPrimaryTask("task-$i") {
						counter.incrementAndGet()
						latch.countDown()
					}
				}
			}

			threads.forEach { it.start() }
			assertTrue(latch.await(5, TimeUnit.SECONDS), "Not all tasks completed in time")
			threads.forEach { it.join() }

			assertEquals(actual = counter.get(), expected = taskCount)
			dispatcher.shutdown(timeout = 5.seconds)
		}
	}


	@Test
	fun `rapid primary and background interleaving completes all tasks`() = runTest {
		repeat(50) {
			val dispatcher = ServiceDispatcher(serviceName = "test")
			dispatcher.start()

			val primaryCount = 50
			val backgroundCount = 50
			val counter = AtomicInteger(0)
			val latch = CountDownLatch(primaryCount + backgroundCount)

			val threads = mutableListOf<Thread>()

			for (i in 1 .. primaryCount) {
				threads.add(Thread {
					dispatcher.launchPrimaryTask("p-$i") {
						counter.incrementAndGet()
						latch.countDown()
					}
				})
			}

			for (i in 1 .. backgroundCount) {
				threads.add(Thread {
					runBlocking {
						dispatcher.launchBackgroundTask("b-$i") {
							counter.incrementAndGet()
							latch.countDown()
						}
					}
				})
			}

			threads.shuffle()
			threads.forEach { it.start() }
			assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all tasks completed in time")
			threads.forEach { it.join() }

			assertEquals(actual = counter.get(), expected = primaryCount + backgroundCount)
			dispatcher.shutdown(timeout = 5.seconds)
		}
	}


	@Test
	fun `single-execution invariant holds under high contention`() = runTest {
		repeat(100) {
			val dispatcher = ServiceDispatcher(serviceName = "test")
			dispatcher.start()

			val taskCount = 200
			val maxConcurrency = AtomicInteger(0)
			val currentConcurrency = AtomicInteger(0)
			val latch = CountDownLatch(taskCount)

			for (i in 1 .. taskCount) {
				val block: suspend CoroutineScope.() -> Unit = {
					// Measure concurrency within each dispatch slice (not spanning yield).
					repeat(3) {
						val level = currentConcurrency.incrementAndGet()
						maxConcurrency.updateAndGet { max -> maxOf(max, level) }
						currentConcurrency.decrementAndGet()
						yield()
					}
					latch.countDown()
				}

				if (i % 2 == 0)
					dispatcher.launchPrimaryTask("task-$i", block)
				else
					dispatcher.launchBackgroundTask("task-$i", block)
			}

			assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all tasks completed in time")

			assertEquals(
				actual = maxConcurrency.get(),
				expected = 1,
				message = "Single-execution invariant violated: max concurrency was ${maxConcurrency.get()}",
			)

			dispatcher.shutdown(timeout = 5.seconds)
		}
	}


	@Test
	fun `no dispatch is lost under rapid enqueue-dequeue cycling`() = runTest {
		repeat(100) {
			val dispatcher = ServiceDispatcher(serviceName = "test")
			dispatcher.start()

			val taskCount = 100
			val counter = AtomicInteger(0)
			val latch = CountDownLatch(taskCount)

			for (i in 1 .. taskCount) {
				dispatcher.launchPrimaryTask("task-$i") {
					yield()
					counter.incrementAndGet()
					latch.countDown()
				}
			}

			assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all tasks completed — possible lost dispatch")

			assertEquals(actual = counter.get(), expected = taskCount)
			dispatcher.shutdown(timeout = 5.seconds)
		}
	}

	// endregion


	// region shutdown

	@Test
	fun `shutdown waits for in-flight primary task`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val started = CompletableDeferred<Unit>()
		var taskFinished = false

		dispatcher.launchPrimaryTask("task") {
			started.complete(Unit)
			yield()
			taskFinished = true
		}

		started.await()
		dispatcher.shutdown(timeout = 5.seconds)

		assertTrue(taskFinished)
	}


	@Test
	fun `shutdown cancels tasks after timeout`() = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val started = CompletableDeferred<Unit>()
		var taskFinished = false

		dispatcher.launchPrimaryTask("stuck") {
			started.complete(Unit)
			delay(Duration.INFINITE)
			taskFinished = true
		}

		started.await()
		dispatcher.shutdown(timeout = 50.milliseconds)

		assertFalse(taskFinished)
	}


	@Test
	fun `shutdown clears all queues on timeout`(): Unit = runTest {
		val dispatcher = ServiceDispatcher(serviceName = "test")
		dispatcher.start()

		val blockingStarted = CompletableDeferred<Unit>()

		dispatcher.launchPrimaryTask("blocking") {
			blockingStarted.complete(Unit)
			delay(Duration.INFINITE)
		}

		blockingStarted.await()

		dispatcher.launchPrimaryTask("queued-primary") { }
		dispatcher.launchBackgroundTask("queued-bg") { }

		dispatcher.shutdown(timeout = 50.milliseconds)

		assertFailsWith<IllegalStateException> {
			dispatcher.launchPrimaryTask("after-shutdown") {}
		}
	}

	// endregion
}
