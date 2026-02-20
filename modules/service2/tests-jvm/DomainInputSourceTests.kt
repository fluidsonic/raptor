package io.fluidsonic.raptor.service2

import kotlin.test.*


/**
 * Tests for domain-related input sources.
 */
class DomainInputSourceTests {

	@Test
	fun `DefaultAggregateChangesInputSource stores changes`() {
		val changes = setOf(TestChange::class)

		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(changes = changes, idClass = TestId::class)

		assertEquals(actual = source.changes, expected = changes)
		assertFalse(source.includesHistory)
	}


	@Test
	fun `DefaultAggregateChangesInputSource includingHistory returns copy with flag`() {
		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange::class),
			idClass = TestId::class,
		)

		val withHistory = source.includingHistory()

		assertTrue(withHistory.includesHistory)
		assertFalse(source.includesHistory) // Original unchanged
	}


	@Test
	fun `DefaultAggregateChangesInputSource includingHistory returns same instance when already including`() {
		val source = DefaultAggregateChangesInputSource<TestService, TestId, TestChange>(
			changes = setOf(TestChange::class),
			idClass = TestId::class,
			includesHistory = true,
		)

		val withHistory = source.includingHistory()

		assertSame(actual = withHistory, expected = source)
	}


	@Test
	fun `AggregateProjectionChangesInputSource stores projection and changes`() {
		val changes = setOf(TestChange::class)

		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = changes,
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		assertEquals(actual = source.changes, expected = changes)
		assertEquals(actual = source.projection, expected = TestProjection::class)
		assertFalse(source.includesHistory)
	}


	@Test
	fun `AggregateProjectionChangesInputSource includingHistory returns copy with flag`() {
		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange::class),
			idClass = TestId::class,
			projection = TestProjection::class,
		)

		val withHistory = source.includingHistory()

		assertTrue(withHistory.includesHistory)
		assertFalse(source.includesHistory) // Original unchanged
	}


	@Test
	fun `AggregateProjectionChangesInputSource includingHistory returns same instance when already including`() {
		val source = AggregateProjectionChangesInputSource<TestService, TestId, TestChange, TestProjection>(
			changes = setOf(TestChange::class),
			idClass = TestId::class,
			includesHistory = true,
			projection = TestProjection::class,
		)

		val withHistory = source.includingHistory()

		assertSame(actual = withHistory, expected = source)
	}


	@Test
	fun `DefaultAggregatesLoadedInputSource is singleton`() {
		val source1 = DefaultAggregatesLoadedInputSource
		val source2 = DefaultAggregatesLoadedInputSource

		assertSame(source1, source2)
	}
}
