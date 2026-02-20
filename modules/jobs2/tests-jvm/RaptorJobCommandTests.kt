package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*
import kotlin.test.*


class RaptorJobCommandTests {

	private val t1 = Timestamp.fromEpochMilliseconds(1000)
	private val execId = JobExecutionId("exec-1")


	@Test
	fun `Create carries description, input, and timestamp`() {
		val desc = TestDescription()
		val command = RaptorJobCommand.Create(description = desc, input = "data", timestamp = t1)

		assertSame(actual = command.description, expected = desc)
		assertEquals(actual = command.input, expected = "data")
		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `Cancel carries timestamp`() {
		val command = RaptorJobCommand.Cancel(timestamp = t1)

		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `CancelExecution carries id and timestamp`() {
		val command = RaptorJobCommand.CancelExecution(id = execId, timestamp = t1)

		assertEquals(actual = command.id, expected = execId)
		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `FailExecution carries message, retryable, id, and timestamp`() {
		val command = RaptorJobCommand.FailExecution(message = "boom", retryable = false, id = execId, timestamp = t1)

		assertEquals(actual = command.message, expected = "boom")
		assertEquals(actual = command.retryable, expected = false)
		assertEquals(actual = command.id, expected = execId)
		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `FailExecution defaults - null message and retryable true`() {
		val command = RaptorJobCommand.FailExecution(id = execId, timestamp = t1)

		assertNull(command.message)
		assertEquals(actual = command.retryable, expected = true)
	}


	@Test
	fun `StartExecution carries id and timestamp`() {
		val command = RaptorJobCommand.StartExecution(id = execId, timestamp = t1)

		assertEquals(actual = command.id, expected = execId)
		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `SucceedExecution carries id, output, and timestamp`() {
		val command = RaptorJobCommand.SucceedExecution(id = execId, output = 42, timestamp = t1)

		assertEquals(actual = command.id, expected = execId)
		assertEquals(actual = command.output, expected = 42)
		assertEquals(actual = command.timestamp, expected = t1)
	}


	@Test
	fun `equality for data classes`() {
		val a = RaptorJobCommand.StartExecution(id = execId, timestamp = t1)
		val b = RaptorJobCommand.StartExecution(id = execId, timestamp = t1)

		assertEquals(actual = a, expected = b)
	}


	private class TestDescription : RaptorJobDescription<String, Unit> {
		override val inputSerializer get() = error("Not needed for tests")
		override val id = JobDescriptionId<String, Unit>("test")
		override val outputSerializer get() = error("Not needed for tests")
	}
}
