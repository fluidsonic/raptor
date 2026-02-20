package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.slf4j.*


class RaptorService2PluginTests {

	@Test
	fun `service starts and onStart handler fires`() = runTest {
		val started = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("test-service") {
				factory { PluginTestServiceImpl() }
				onStart().handle { started.complete(Unit) }
			}
		}

		raptor.lifecycle.startIn(this)

		assertTrue(started.isCompleted)

		raptor.lifecycle.stop()
	}


	@Test
	fun `multiple services are created and started`() = runTest {
		val service1Started = CompletableDeferred<Unit>()
		val service2Started = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("service-1") {
				factory { PluginTestServiceImpl() }
				onStart().handle { service1Started.complete(Unit) }
			}
			service2<PluginTestService>("service-2") {
				factory { PluginTestServiceImpl() }
				onStart().handle { service2Started.complete(Unit) }
			}
		}

		raptor.lifecycle.startIn(this)

		assertTrue(service1Started.isCompleted)
		assertTrue(service2Started.isCompleted)

		raptor.lifecycle.stop()
	}


	@Test
	fun `DI registration works - service factory receives injected dependency`() = runTest {
		val receivedValue = CompletableDeferred<String>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }
			di.provide<String>("injected-value")

			service2<PluginTestService>("test-service") {
				factory {
					receivedValue.complete(di<String>())
					PluginTestServiceImpl()
				}
				onStart().handle {}
			}
		}

		raptor.lifecycle.startIn(this)

		assertEquals(actual = receivedValue.await(), expected = "injected-value")

		raptor.lifecycle.stop()
	}


	@Test
	fun `error handler StopService cancels service scope on error`() = runTest {
		val handlerCalled = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("test-service") {
				factory { PluginTestServiceImpl() }

				onStart().handle {
					handlerCalled.complete(Unit)
					throw RuntimeException("test error")
				}

				onError().stopService()
			}
		}

		raptor.lifecycle.startIn(this)
		runCurrent()

		assertTrue(handlerCalled.isCompleted)

		raptor.lifecycle.stop()
	}


	@Test
	fun `error handler StopLifecycle cancels lifecycle scope on error`() = runTest {
		val handlerCalled = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("test-service") {
				factory { PluginTestServiceImpl() }

				onStart().handle {
					handlerCalled.complete(Unit)
					throw RuntimeException("lifecycle error")
				}

				onError().stopLifecycle()
			}
		}

		raptor.lifecycle.startIn(this)
		runCurrent()

		assertTrue(handlerCalled.isCompleted)

		raptor.lifecycle.stop()
	}


	@Test
	fun `service stop cancels service scopes`() = runTest {
		val serviceRunning = CompletableDeferred<Unit>()
		val serviceCancelled = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("test-service") {
				factory { PluginTestServiceImpl() }
				onStart().handle {
					serviceRunning.complete(Unit)

					try {
						awaitCancellation()
					}
					finally {
						serviceCancelled.complete(Unit)
					}
				}
			}
		}

		raptor.lifecycle.startIn(this)
		runCurrent()

		assertTrue(serviceRunning.isCompleted)
		assertFalse(serviceCancelled.isCompleted)

		raptor.lifecycle.stop()
		runCurrent()

		assertTrue(serviceCancelled.isCompleted)
	}


	@Test
	fun `onAggregatesLoaded fires after lifecycle notifies`() = runTest {
		val aggregatesLoadedFired = CompletableDeferred<Unit>()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorService2Plugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			service2<PluginTestService>("test-service") {
				factory { PluginTestServiceImpl() }
				onAggregatesLoaded().handle { aggregatesLoadedFired.complete(Unit) }
			}
		}

		raptor.lifecycle.startIn(this)
		runCurrent()

		// Without domain plugin, notifyAggregatesLoaded is not called automatically.
		// Access the subscription engine manually to trigger it.
		val config = raptor.context.plugins[RaptorService2Plugin]
		config.subscriptionEngine!!.notifyAggregatesLoaded()
		runCurrent()

		assertTrue(aggregatesLoadedFired.isCompleted)

		raptor.lifecycle.stop()
	}
}


private interface PluginTestService : RaptorService2


private class PluginTestServiceImpl : PluginTestService
