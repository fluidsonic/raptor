package io.fluidsonic.raptor.service2

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.slf4j.*


/**
 * Tests for RaptorServiceController2 lifecycle management.
 *
 * Covers service creation, start, stop, error handling, and edge cases.
 */
class RaptorServiceController2Tests {

	private val logger: Logger = LoggerFactory.getLogger(RaptorServiceController2Tests::class.java)


	// region createIn

	@Test
	fun `createIn makes service available`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.createIn(di, logger)

		assertSame(actual = controller.service, expected = service)
	}


	@Test
	fun `service property throws before createIn`() {
		val diKey = ServiceDIKey2<TestService>("test-service")
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		assertFailsWith<IllegalStateException> {
			controller.service
		}
	}


	@Test
	fun `double createIn throws`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.createIn(di, logger)

		assertFailsWith<IllegalStateException> {
			controller.createIn(di, logger)
		}
	}


	@Test
	fun `createIn sets CurrentServiceWorker during DI resolution`() = runTest {
		var workerDuringCreation: RaptorServiceWorker? = null
		val diKey = ServiceDIKey2<TestService>("test-service")
		val di = TestDI(mapOf(diKey to {
			workerDuringCreation = CurrentServiceWorker.current
			TestServiceImpl()
		}))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.createIn(di, logger)

		assertNotNull(workerDuringCreation)
	}

	// endregion


	// region start

	@Test
	fun `start subscribes to input sources`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		var handlerCalled = false

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = { handlerCalled = true },
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		assertTrue(handlerCalled)
	}


	@Test
	fun `start before createIn throws`() = runTest {
		val diKey = ServiceDIKey2<TestService>("test-service")
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)

		assertFailsWith<IllegalStateException> {
			controller.start(lifecycleScope = this, subscriptionEngine = engine)
		}
	}

	// endregion


	// region stop

	@Test
	fun `stop before createIn does not throw`() = runTest {
		val diKey = ServiceDIKey2<TestService>("test-service")
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.stop()
	}


	@Test
	fun `stop before start does not throw`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.createIn(di, logger)
		controller.stop()

		assertFailsWith<IllegalStateException> {
			controller.service
		}
	}


	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `stop cancels running subscriptions`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		var handlerCompleted = false

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = {
					delay(10_000)
					handlerCompleted = true
				},
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		controller.stop()

		assertFalse(handlerCompleted)
	}


	@Test
	fun `stop clears service reference`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		assertSame(actual = controller.service, expected = service)

		controller.stop()

		assertFailsWith<IllegalStateException> {
			controller.service
		}
	}

	// endregion


	// region error handling

	@Test
	fun `Default error handler logs and continues`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		var secondHandlerCalled = false

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = { throw RuntimeException("test error") },
			),
			RaptorServiceInputRegistration<TestService, Unit>(
				source = DefaultAggregatesLoadedInputSource,
				handler = { secondHandlerCalled = true },
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		// First handler threw, but service should still be running
		engine.notifyAggregatesLoaded()
		runCurrent()

		assertTrue(secondHandlerCalled)
	}


	@Test
	fun `StopService error handler cancels service scope`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		var secondHandlerCalled = false

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = { throw RuntimeException("test error") },
			),
			RaptorServiceInputRegistration<TestService, Unit>(
				source = DefaultAggregatesLoadedInputSource,
				handler = { secondHandlerCalled = true },
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.StopService,
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		// First handler threw, service scope should be cancelled
		engine.notifyAggregatesLoaded()
		runCurrent()

		// Second handler should NOT fire because service scope was cancelled
		assertFalse(secondHandlerCalled)
	}


	@Test
	fun `StopLifecycle error handler cancels lifecycle scope`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val testException = RuntimeException("test error")

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = { throw testException },
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.StopLifecycle,
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)

		val lifecycleScope = CoroutineScope(
			coroutineContext + SupervisorJob(coroutineContext.job)
		)
		controller.start(lifecycleScope = lifecycleScope, subscriptionEngine = engine)

		runCurrent()

		// StopLifecycle should cancel the entire lifecycle scope, not just the service scope
		assertTrue(lifecycleScope.coroutineContext.job.isCancelled)
	}


	@Test
	fun `Custom error handler receives error`() = runTest {
		val service = TestServiceImpl()
		val diKey = ServiceDIKey2<TestService>("test-service")
		val testException = RuntimeException("test error")
		val receivedErrors = mutableListOf<RaptorServiceError2>()

		val inputSources = listOf(
			RaptorServiceInputRegistration<TestService, Unit>(
				source = StartInputSource,
				handler = { throw testException },
			),
		)
		val di = TestDI(mapOf(diKey to { service }))
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Custom { error ->
				receivedErrors.add(error)
			},
			inputSources = inputSources,
			name = "test-service",
		)

		controller.createIn(di, logger)

		val engine = RaptorServiceSubscriptionEngine(TestRaptorContext, logger)
		controller.start(lifecycleScope = this, subscriptionEngine = engine)

		assertEquals(actual = receivedErrors.size, expected = 1)
		assertSame(actual = receivedErrors.first().throwable, expected = testException)
	}

	// endregion


	// region toString

	@Test
	fun `toString includes service name`() {
		val diKey = ServiceDIKey2<TestService>("my-service")
		val controller = RaptorServiceController2(
			diKey = diKey,
			errorHandler = RaptorServiceComponent2.ErrorHandler.Default,
			inputSources = emptyList(),
			name = "my-service",
		)

		assertEquals(actual = controller.toString(), expected = "RaptorServiceController2 'my-service'")
	}

	// endregion


}
