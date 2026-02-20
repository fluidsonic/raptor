package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.*
import kotlin.reflect.full.*
import kotlin.test.*
import kotlinx.coroutines.test.*


/**
 * Tests for RaptorMiddlewareController event dispatching.
 */
class RaptorMiddlewareControllerTests {

	@Test
	fun `dispatches to all middleware in registration order`() = runTest {
		val callOrder = mutableListOf<String>()

		val middlewareA = TestMiddleware { callOrder.add("a") }
		val middlewareB = TestMiddleware { callOrder.add("b") }

		val di = TestDI(mapOf(
			RaptorDIKey<RaptorStreamMiddleware>(RaptorStreamMiddleware::class.starProjectedType) to { middlewareA },
		))

		// Since both registrations use the same type, di.get will return middlewareA for both.
		// To properly test ordering with two different instances, we need distinct types.
		// Instead, test with a single middleware class and verify order through a list-backed DI.
		val middleware = mutableListOf<RaptorStreamMiddleware>()
		middleware.add(middlewareA)
		middleware.add(middlewareB)

		// Use a DI that returns middleware by index
		var resolveCount = 0
		val indexedDi = TestDI(mapOf(
			RaptorDIKey<RaptorStreamMiddleware>(RaptorStreamMiddleware::class.starProjectedType) to {
				middleware[resolveCount++]
			},
		))

		val controller = RaptorMiddlewareController(
			di = indexedDi,
			registrations = listOf(
				MiddlewareRegistration(RaptorStreamMiddleware::class),
				MiddlewareRegistration(RaptorStreamMiddleware::class),
			),
		)

		controller.process(testEvent)

		assertEquals(actual = callOrder, expected = listOf("a", "b"))
	}


	@Test
	fun `empty middleware list does nothing`() = runTest {
		val di = TestDI(emptyMap())

		val controller = RaptorMiddlewareController(
			di = di,
			registrations = emptyList(),
		)

		// Should not throw
		controller.process(testEvent)
	}


	@Test
	fun `middleware exception propagates`() = runTest {
		val testException = RuntimeException("middleware error")

		val middleware = TestMiddleware { throw testException }

		val di = TestDI(mapOf(
			RaptorDIKey<RaptorStreamMiddleware>(RaptorStreamMiddleware::class.starProjectedType) to { middleware },
		))

		val controller = RaptorMiddlewareController(
			di = di,
			registrations = listOf(
				MiddlewareRegistration(RaptorStreamMiddleware::class),
			),
		)

		val thrown = assertFailsWith<RuntimeException> {
			controller.process(testEvent)
		}
		assertSame(actual = thrown, expected = testException)
	}


	private class TestMiddleware(
		private val onIntercept: suspend () -> Unit,
	) : RaptorStreamMiddleware {

		override suspend fun intercept(event: RaptorAggregateProjectionEvent<*, *, *>) {
			onIntercept()
		}
	}


	companion object {

		private val testEvent = RaptorAggregateProjectionEvent<TestId, TestProjection, TestChange>(
			change = TestChange.Created("test"),
			id = RaptorAggregateEventId(1),
			projection = TestProjectionImpl(TestId("test"), "test"),
			timestamp = Timestamp.fromEpochMilliseconds(0),
			version = 1,
		)
	}
}
