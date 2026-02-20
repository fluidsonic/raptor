package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*


class AggregateSubscriptionTests {

	private val logger: Logger = LoggerFactory.getLogger(AggregateSubscriptionTests::class.java)


	// region DefaultAggregateChangesInputSource

	@Test
	fun `aggregate changes - matching events are delivered`() = runTest {
		val eventSource = TestAggregateEventSource()
		val engine = createEngineWithEventSources(aggregateEventSource = eventSource)
		val service = TestServiceImpl()
		val results = mutableListOf<RaptorAggregateEvent<*, *>>()

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = { results.add(it) },
		)

		val event = testAggregateEvent(TestId("1"), TestChange.Created("foo"))
		eventSource.emit(event)
		runCurrent()

		assertEquals(actual = results.size, expected = 1)
		assertSame(actual = results.first(), expected = event)
	}


	@Test
	fun `aggregate changes - subscribes with replay true when includesHistory is true`() = runTest {
		val eventSource = TestAggregateEventSource()
		val engine = createEngineWithEventSources(aggregateEventSource = eventSource)
		val service = TestServiceImpl()

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = {},
		)

		assertEquals(actual = eventSource.subscriptions.size, expected = 1)
		val sub = eventSource.subscriptions.first()
		assertTrue(sub.replay)
		assertEquals(actual = sub.changeClasses, expected = setOf(TestChange.Created::class))
		assertEquals(actual = sub.idClass, expected = TestId::class)
	}


	@Test
	fun `aggregate changes - subscribes with replay false when includesHistory is false`() = runTest {
		val eventSource = TestAggregateEventSource()
		val engine = createEngineWithEventSources(aggregateEventSource = eventSource)
		val service = TestServiceImpl()

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = false,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = {},
		)

		assertEquals(actual = eventSource.subscriptions.size, expected = 1)
		assertFalse(eventSource.subscriptions.first().replay)
	}


	@Test
	fun `aggregate changes - error in handler propagates to error handler`() = runTest {
		val eventSource = TestAggregateEventSource()
		val engine = createEngineWithEventSources(aggregateEventSource = eventSource)
		val service = TestServiceImpl()
		val errors = mutableListOf<RaptorService2.Error>()
		val testException = RuntimeException("test error")

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = { throw testException },
		)

		eventSource.emit(testAggregateEvent(TestId("1"), TestChange.Created("foo")))
		runCurrent()

		assertEquals(actual = errors.size, expected = 1)
		assertSame(actual = errors.first().throwable, expected = testException)
	}


	@Test
	fun `aggregate changes - includingHistory returns copy with includesHistory true`() {
		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = false,
		)

		val withHistory = source.includingHistory() as DefaultAggregateChangesInputSource
		assertTrue(withHistory.includesHistory)
		assertEquals(actual = withHistory.changes, expected = source.changes)
		assertEquals(actual = withHistory.idClass, expected = source.idClass)
	}


	@Test
	fun `aggregate changes - includingHistory on already-including source returns same instance`() {
		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		assertSame(actual = source.includingHistory(), expected = source)
	}


	@Test
	fun `aggregate changes - multiple events delivered in order`() = runTest {
		val eventSource = TestAggregateEventSource()
		val engine = createEngineWithEventSources(aggregateEventSource = eventSource)
		val service = TestServiceImpl()
		val results = mutableListOf<RaptorAggregateEvent<*, *>>()

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange.Created::class, TestChange.Updated::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = { results.add(it) },
		)

		val event1 = testAggregateEvent(TestId("1"), TestChange.Created("foo"))
		val event2 = testAggregateEvent(TestId("2"), TestChange.Updated("bar"))

		eventSource.emit(event1)
		eventSource.emit(event2)
		runCurrent()

		assertEquals(actual = results.size, expected = 2)
		assertSame(actual = results[0], expected = event1)
		assertSame(actual = results[1], expected = event2)
	}

	// endregion


	// region AggregateProjectionChangesInputSource

	@Test
	fun `projection changes - matching events are delivered`() = runTest {
		val eventSource = TestAggregateProjectionEventSource()
		val engine = createEngineWithEventSources(aggregateProjectionEventSource = eventSource)
		val service = TestServiceImpl()
		val results = mutableListOf<RaptorAggregateProjectionEvent<*, *, *>>()

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = { results.add(it) },
		)

		val event = testProjectionEvent(TestId("1"), TestChange.Created("foo"), TestProjectionImpl(TestId("1"), "foo"))
		eventSource.emit(event)
		runCurrent()

		assertEquals(actual = results.size, expected = 1)
		assertSame(actual = results.first(), expected = event)
	}


	@Test
	fun `projection changes - subscribes with correct projection class`() = runTest {
		val eventSource = TestAggregateProjectionEventSource()
		val engine = createEngineWithEventSources(aggregateProjectionEventSource = eventSource)
		val service = TestServiceImpl()

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = {},
		)

		assertEquals(actual = eventSource.subscriptions.size, expected = 1)
		val sub = eventSource.subscriptions.first()
		assertEquals(actual = sub.projectionClass, expected = TestProjection::class)
		assertEquals(actual = sub.idClass, expected = TestId::class)
		assertEquals(actual = sub.changeClasses, expected = setOf(TestChange.Created::class))
	}


	@Test
	fun `projection changes - subscribes with replay true when includesHistory is true`() = runTest {
		val eventSource = TestAggregateProjectionEventSource()
		val engine = createEngineWithEventSources(aggregateProjectionEventSource = eventSource)
		val service = TestServiceImpl()

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = true,
			projection = TestProjection::class,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = {},
		)

		assertTrue(eventSource.subscriptions.first().replay)
	}


	@Test
	fun `projection changes - subscribes with replay false when includesHistory is false`() = runTest {
		val eventSource = TestAggregateProjectionEventSource()
		val engine = createEngineWithEventSources(aggregateProjectionEventSource = eventSource)
		val service = TestServiceImpl()

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			includesHistory = false,
			projection = TestProjection::class,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = {},
		)

		assertFalse(eventSource.subscriptions.first().replay)
	}


	@Test
	fun `projection changes - error in handler propagates to error handler`() = runTest {
		val eventSource = TestAggregateProjectionEventSource()
		val engine = createEngineWithEventSources(aggregateProjectionEventSource = eventSource)
		val service = TestServiceImpl()
		val errors = mutableListOf<RaptorService2.Error>()
		val testException = RuntimeException("projection error")

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		engine.subscribe(
			service = service,
			source = source,
			handler = { throw testException },
		)

		eventSource.emit(testProjectionEvent(TestId("1"), TestChange.Created("foo"), TestProjectionImpl(TestId("1"), "foo")))
		runCurrent()

		assertEquals(actual = errors.size, expected = 1)
		assertSame(actual = errors.first().throwable, expected = testException)
	}


	@Test
	fun `projection changes - includingHistory returns copy with includesHistory true`() {
		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange.Created::class),
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		val withHistory = source.includingHistory() as AggregateProjectionChangesInputSource
		assertTrue(withHistory.includesHistory)
		assertEquals(actual = withHistory.projection, expected = TestProjection::class)
	}

	// endregion


	// region Test infrastructure

	private fun createEngineWithEventSources(
		aggregateEventSource: RaptorAggregateEventSource? = null,
		aggregateProjectionEventSource: RaptorAggregateProjectionEventSource? = null,
	): RaptorServiceSubscriptionEngine =
		RaptorServiceSubscriptionEngine(
			context = TestRaptorContext,
			logger = logger,
			aggregateEventSource = aggregateEventSource,
			aggregateProjectionEventSource = aggregateProjectionEventSource,
		)

	// endregion
}


// region Test aggregate types

internal interface TestId : RaptorAggregateProjectionId {

	val value: String

	override val discriminator: String get() = "test-id"

	companion object {

		operator fun invoke(value: String): TestId = TestIdImpl(value)
	}
}


private data class TestIdImpl(override val value: String) : TestId


internal sealed class TestChange : RaptorAggregateChange<TestId> {

	data class Created(val name: String) : TestChange()
	data class Updated(val name: String) : TestChange()
}


internal interface TestProjection : RaptorAggregateProjection<TestId>


internal data class TestProjectionImpl(override val id: TestId, val name: String) : TestProjection

// endregion


// region Test event sources

internal class TestAggregateEventSource : RaptorAggregateEventSource {

	val subscriptions = mutableListOf<AggregateSubscription>()
	private val handlers = mutableListOf<suspend (RaptorAggregateEvent<*, *>) -> Unit>()


	context(coroutineScope: CoroutineScope)
	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> subscribe(
		handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		changeClasses: Set<KClass<out Change>>,
		idClass: KClass<Id>,
		async: Boolean,
		replay: Boolean,
	): Job {
		subscriptions.add(
			AggregateSubscription(
				changeClasses = changeClasses as Set<KClass<*>>,
				idClass = idClass,
				replay = replay,
			)
		)
		handlers.add(handler as suspend (RaptorAggregateEvent<*, *>) -> Unit)

		return Job(parent = coroutineScope.coroutineContext.job)
	}


	context(coroutineScope: CoroutineScope)
	override fun subscribe(handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit, async: Boolean): Job =
		Job(parent = coroutineScope.coroutineContext.job)


	suspend fun emit(event: RaptorAggregateEvent<*, *>) {
		for (handler in handlers) handler(event)
	}


	data class AggregateSubscription(
		val changeClasses: Set<KClass<*>>,
		val idClass: KClass<*>,
		val replay: Boolean,
	)
}


internal class TestAggregateProjectionEventSource : RaptorAggregateProjectionEventSource {

	val subscriptions = mutableListOf<ProjectionSubscription>()
	private val handlers = mutableListOf<suspend (RaptorAggregateProjectionEvent<*, *, *>) -> Unit>()


	context(coroutineScope: CoroutineScope)
	override fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>> subscribe(
		handler: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
		changeClasses: Set<KClass<out Change>>,
		idClass: KClass<Id>,
		projectionClass: KClass<out Projection>,
		async: Boolean,
		replay: Boolean,
	): Job {
		subscriptions.add(
			ProjectionSubscription(
				changeClasses = changeClasses as Set<KClass<*>>,
				idClass = idClass,
				projectionClass = projectionClass,
				replay = replay,
			)
		)
		handlers.add(handler as suspend (RaptorAggregateProjectionEvent<*, *, *>) -> Unit)

		return Job(parent = coroutineScope.coroutineContext.job)
	}


	context(coroutineScope: CoroutineScope)
	override fun subscribe(handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit, async: Boolean): Job =
		Job(parent = coroutineScope.coroutineContext.job)


	suspend fun emit(event: RaptorAggregateProjectionEvent<*, *, *>) {
		for (handler in handlers) handler(event)
	}


	data class ProjectionSubscription(
		val changeClasses: Set<KClass<*>>,
		val idClass: KClass<*>,
		val projectionClass: KClass<*>,
		val replay: Boolean,
	)
}

// endregion


// region Test helpers

private var nextEventId = 1L

internal fun testAggregateEvent(
	id: TestId,
	change: TestChange,
	version: Int = 1,
): RaptorAggregateEvent<TestId, TestChange> =
	RaptorAggregateEvent(
		aggregateId = id,
		change = change,
		id = RaptorAggregateEventId(nextEventId++),
		timestamp = Instant.fromEpochSeconds(0),
		version = version,
	)


internal fun testProjectionEvent(
	id: TestId,
	change: TestChange,
	projection: TestProjection,
	version: Int = 1,
): RaptorAggregateProjectionEvent<TestId, TestProjection, TestChange> =
	RaptorAggregateProjectionEvent(
		change = change,
		id = RaptorAggregateEventId(nextEventId++),
		projection = projection,
		timestamp = Instant.fromEpochSeconds(0),
		version = version,
	)

// endregion
