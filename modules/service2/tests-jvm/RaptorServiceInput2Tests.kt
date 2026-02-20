package io.fluidsonic.raptor.service2

import io.fluidsonic.time.*
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds


/**
 * Tests for RaptorServiceInput2 operators.
 * These tests verify the operator functions create the correct input source types.
 */
class RaptorServiceInput2Tests {

	@Test
	fun `batchBy creates BatchByInputSource`() {
		val source = StartInputSource
		val timeout = 5.seconds
		val keySelector: context(RaptorService2) (Unit) -> String = { "key" }

		val result = source.batchBy(timeout, keySelector)

		assertTrue(result is BatchByInputSource<*, *>)
		val batchSource = result as BatchByInputSource<RaptorService2, Unit>
		assertSame(actual = batchSource.source, expected = source)
		assertEquals(actual = batchSource.batchTimeout, expected = timeout)
	}


	@Test
	fun `delay creates DelayInputSource`() {
		val source = StartInputSource
		val duration = 5.seconds

		val result = source.delay(duration)

		assertTrue(result is DelayInputSource<RaptorService2, *>)
		val delaySource = result as DelayInputSource<RaptorService2, Unit>
		assertSame(actual = delaySource.source, expected = source)
		assertEquals(actual = delaySource.duration, expected = duration)
	}


	@Test
	fun `delayUntil creates DelayUntilInputSource`() {
		val source = StartInputSource
		val timestampSelector: context(RaptorService2) (Unit) -> Timestamp = { Timestamp.fromEpochMilliseconds(0) }

		val result = source.delayUntil(timestampSelector)

		assertTrue(result is DelayUntilInputSource<RaptorService2, *>)
		val delayUntilSource = result as DelayUntilInputSource<RaptorService2, Unit>
		assertSame(actual = delayUntilSource.source, expected = source)
	}


	@Test
	fun `waitFor creates WaitingInputSource`() {
		val source = StartInputSource
		val waitForSource = StartInputSource

		val result = source.waitFor(waitForSource)

		assertTrue(result is WaitingInputSource<RaptorService2, *>)
		val waitingSource = result as WaitingInputSource<RaptorService2, Unit>
		assertSame(actual = waitingSource.source, expected = source)
		assertSame(actual = waitingSource.sourceToWaitFor, expected = waitForSource)
	}


	@Test
	fun `chaining operators creates nested sources`() {
		val baseSource = StartInputSource
		val duration = 5.seconds
		val timeout = 1.seconds

		// Chain: start -> delay -> batchBy
		val result = baseSource
			.delay(duration)
			.batchBy(timeout) { "key" }

		// Verify structure
		assertTrue(result is BatchByInputSource<RaptorService2, *>)
		val batchSource = result as BatchByInputSource<RaptorService2, *>

		assertTrue(batchSource.source is DelayInputSource<RaptorService2, *>)
		val delaySource = batchSource.source as DelayInputSource<RaptorService2, *>

		assertSame(actual = delaySource.source, expected = baseSource)
	}


	@Test
	fun `StartInputSource is singleton`() {
		val source1 = StartInputSource
		val source2 = StartInputSource

		assertSame(source1, source2)
	}


	@Test
	fun `FilteredInputSource stores predicate and source`() {
		val source = StartInputSource
		val predicate: suspend TestService.(Unit) -> Boolean = { true }

		val filtered = FilteredInputSource(predicate, source)

		assertSame(actual = filtered.source, expected = source)
	}


	@Test
	fun `TransformedInputSource stores transform and source`() {
		val source = StartInputSource
		val transform: suspend TestService.(Unit) -> String = { "transformed" }

		val transformed = TransformedInputSource(source, transform)

		assertSame(actual = transformed.source, expected = source)
	}


	@Test
	fun `MapNotNullInputSource stores transform and source`() {
		val source = StartInputSource
		val transform: suspend TestService.(Unit) -> String? = { "value" }

		val mapNotNull = MapNotNullInputSource(source, transform)

		assertSame(actual = mapNotNull.source, expected = source)
	}


	@Test
	fun `FlatMapIterableInputSource stores transform and source`() {
		val source = StartInputSource
		val transform: suspend TestService.(Unit) -> List<String> = { listOf("a", "b") }

		val flatMap = FlatMapIterableInputSource(source, transform)

		assertSame(actual = flatMap.source, expected = source)
	}


}
