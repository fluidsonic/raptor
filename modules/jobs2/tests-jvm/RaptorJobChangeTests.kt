package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*
import kotlin.test.*


class RaptorJobChangeTests {

	private val t1 = Timestamp.fromEpochMilliseconds(1000)


	@Test
	fun `Created carries description, input, and timestamp`() {
		val desc = TestDescription()
		val change = RaptorJobChange.Created(description = desc, input = "data", timestamp = t1)

		assertSame(actual = change.description, expected = desc)
		assertEquals(actual = change.input, expected = "data")
		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `Canceled carries timestamp`() {
		val change = RaptorJobChange.Canceled(timestamp = t1)

		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `ExecutionCanceled carries timestamp`() {
		val change = RaptorJobChange.ExecutionCanceled(timestamp = t1)

		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `ExecutionFailed carries message, retryable, and timestamp`() {
		val change = RaptorJobChange.ExecutionFailed(message = "boom", retryable = false, timestamp = t1)

		assertEquals(actual = change.message, expected = "boom")
		assertEquals(actual = change.retryable, expected = false)
		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `ExecutionFailed defaults - null message and retryable true`() {
		val change = RaptorJobChange.ExecutionFailed(timestamp = t1)

		assertNull(change.message)
		assertEquals(actual = change.retryable, expected = true)
	}


	@Test
	fun `ExecutionStarted carries timestamp`() {
		val change = RaptorJobChange.ExecutionStarted(timestamp = t1)

		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `ExecutionSucceeded carries output and timestamp`() {
		val change = RaptorJobChange.ExecutionSucceeded(output = 42, timestamp = t1)

		assertEquals(actual = change.output, expected = 42)
		assertEquals(actual = change.timestamp, expected = t1)
	}


	@Test
	fun `equality for data classes`() {
		val a = RaptorJobChange.ExecutionStarted(timestamp = t1)
		val b = RaptorJobChange.ExecutionStarted(timestamp = t1)

		assertEquals(actual = a, expected = b)
	}


	private class TestDescription : RaptorJobDescription<String, Unit> {
		override val inputSerializer get() = error("Not needed for tests")
		override val id = JobDescriptionId<String, Unit>("test")
		override val outputSerializer get() = error("Not needed for tests")
	}
}
