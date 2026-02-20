package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*
import kotlin.test.*


class RaptorJobStatusTests {

	private val t0 = Timestamp.fromEpochMilliseconds(0)
	private val t1 = Timestamp.fromEpochMilliseconds(1000)
	private val t2 = Timestamp.fromEpochMilliseconds(2000)
	private val t3 = Timestamp.fromEpochMilliseconds(3000)


	// region Non-canceled jobs

	@Test
	fun `no executions - status is Pending`() {
		val job = testJob<Nothing>(executions = emptyList())

		assertEquals(actual = job.status, expected = RaptorJobStatus.Pending)
	}


	@Test
	fun `last execution started - status is Running`() {
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Started),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Running>(status)
		assertSame(actual = status.executionStatus, expected = RaptorJobExecutionStatus.Started)
	}


	@Test
	fun `last execution succeeded - status is Succeeded`() {
		val executionStatus = RaptorJobExecutionStatus.Succeeded(output = "result", timestamp = t1)
		val job = testJob(
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Succeeded<*>>(status)
		assertSame(actual = status.executionStatus, expected = executionStatus)
	}


	@Test
	fun `last execution failed - status is Failed`() {
		val executionStatus = RaptorJobExecutionStatus.Failed(timestamp = t1)
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Failed>(status)
		assertSame(actual = status.executionStatus, expected = executionStatus)
	}


	@Test
	fun `canceled execution without cancellation request - throws`() {
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Canceled(timestamp = t1)),
			),
		)

		val error = assertFailsWith<IllegalStateException> {
			job.status
		}
		assertEquals(
			actual = error.message,
			expected = "Job test-job has a canceled execution but no cancellation request.",
		)
	}

	// endregion


	// region Canceled jobs

	@Test
	fun `canceled with no executions - status is Canceled`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t1,
			executions = emptyList(),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t1)
	}


	@Test
	fun `canceled with started execution - status is Canceling`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t1,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Started),
			),
		)

		assertEquals(actual = job.status, expected = RaptorJobStatus.Canceling)
	}


	@Test
	fun `canceled with succeeded execution - status is Succeeded`() {
		val executionStatus = RaptorJobExecutionStatus.Succeeded(output = "done", timestamp = t1)
		val job = testJob(
			cancellationRequestTimestamp = t0,
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Succeeded<*>>(status)
		assertSame(actual = status.executionStatus, expected = executionStatus)
	}


	@Test
	fun `canceled with canceled execution - uses later timestamp`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t1,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Canceled(timestamp = t2)),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t2)
	}


	@Test
	fun `canceled with canceled execution - cancellation request timestamp used when later`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t2,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Canceled(timestamp = t1)),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t2)
	}


	@Test
	fun `canceled with canceled execution - same timestamp`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t1,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Canceled(timestamp = t1)),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t1)
	}


	@Test
	fun `canceled with failed execution - status is Canceled with later timestamp`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t1,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Failed(timestamp = t2)),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t2)
	}


	@Test
	fun `canceled with failed execution - cancellation request timestamp used when later`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t2,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Failed(timestamp = t1)),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Canceled>(status)
		assertEquals(actual = status.timestamp, expected = t2)
	}

	// endregion


	// region Multiple executions

	@Test
	fun `multiple executions - uses last execution status`() {
		val lastStatus = RaptorJobExecutionStatus.Succeeded(output = "final", timestamp = t2)
		val job = testJob(
			executions = listOf(
				testExecution(id = "exec-1", status = RaptorJobExecutionStatus.Failed(timestamp = t1)),
				testExecution(id = "exec-2", status = lastStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Succeeded<*>>(status)
		assertSame(actual = status.executionStatus, expected = lastStatus)
	}


	@Test
	fun `multiple executions with cancellation - last succeeded wins`() {
		val lastStatus = RaptorJobExecutionStatus.Succeeded(output = "final", timestamp = t3)
		val job = testJob(
			cancellationRequestTimestamp = t1,
			executions = listOf(
				testExecution(id = "exec-1", status = RaptorJobExecutionStatus.Failed(timestamp = t1)),
				testExecution(id = "exec-2", status = lastStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Succeeded<*>>(status)
		assertSame(actual = status.executionStatus, expected = lastStatus)
	}

	// endregion


	// region outputOrNull

	@Test
	fun `outputOrNull returns output when succeeded`() {
		val job = testJob(
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Succeeded(output = "value", timestamp = t1)),
			),
		)

		assertEquals(actual = job.outputOrNull(), expected = "value")
	}


	@Test
	fun `outputOrNull returns null when no executions`() {
		val job = testJob<Nothing>(executions = emptyList())

		assertNull(job.outputOrNull())
	}


	@Test
	fun `outputOrNull returns null when last execution failed`() {
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Failed(timestamp = t1)),
			),
		)

		assertNull(job.outputOrNull())
	}


	@Test
	fun `outputOrNull returns null when last execution is started`() {
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Started),
			),
		)

		assertNull(job.outputOrNull())
	}


	@Test
	fun `outputOrNull returns null when last execution is canceled`() {
		val job = testJob<Nothing>(
			cancellationRequestTimestamp = t0,
			executions = listOf(
				testExecution(status = RaptorJobExecutionStatus.Canceled(timestamp = t1)),
			),
		)

		assertNull(job.outputOrNull())
	}

	// endregion


	// region Failure info

	@Test
	fun `failed status carries message and retryable from execution`() {
		val executionStatus = RaptorJobExecutionStatus.Failed(
			message = "connection timeout",
			retryable = true,
			timestamp = t1,
		)
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Failed>(status)
		assertEquals(actual = status.executionStatus.message, expected = "connection timeout")
		assertEquals(actual = status.executionStatus.retryable, expected = true)
	}


	@Test
	fun `failed status with non-retryable failure`() {
		val executionStatus = RaptorJobExecutionStatus.Failed(
			message = "invalid input",
			retryable = false,
			timestamp = t1,
		)
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Failed>(status)
		assertEquals(actual = status.executionStatus.message, expected = "invalid input")
		assertEquals(actual = status.executionStatus.retryable, expected = false)
	}


	@Test
	fun `failed status defaults - null message and retryable true`() {
		val executionStatus = RaptorJobExecutionStatus.Failed(timestamp = t1)
		val job = testJob<Nothing>(
			executions = listOf(
				testExecution(status = executionStatus),
			),
		)

		val status = job.status
		assertIs<RaptorJobStatus.Failed>(status)
		assertNull(status.executionStatus.message)
		assertEquals(actual = status.executionStatus.retryable, expected = true)
	}

	// endregion


	// region Test helpers

	private fun <Output> testJob(
		cancellationRequestTimestamp: Timestamp? = null,
		executions: List<RaptorJobExecution<Output>>,
	): RaptorJob<Unit, Output> = TestJob(
		cancellationRequestTimestamp = cancellationRequestTimestamp,
		executions = executions,
	)


	private fun <Output> testExecution(
		id: String = "exec-1",
		status: RaptorJobExecutionStatus<Output>,
	): RaptorJobExecution<Output> = TestExecution(
		id = JobExecutionId(id),
		startTimestamp = t0,
		status = status,
	)


	private data class TestJob<Output>(
		override val cancellationRequestTimestamp: Timestamp?,
		override val creationTimestamp: Timestamp = Timestamp.fromEpochMilliseconds(0),
		override val description: RaptorJobDescription<Unit, Output> = TestDescription(),
		override val executions: List<RaptorJobExecution<Output>>,
		override val id: RaptorJobId<Unit, Output> = RaptorJobId("test-job"),
		override val input: Unit = Unit,
	) : RaptorJob<Unit, Output>


	private data class TestExecution<Output>(
		override val id: JobExecutionId,
		override val startTimestamp: Timestamp,
		override val status: RaptorJobExecutionStatus<Output>,
	) : RaptorJobExecution<Output>


	private class TestDescription<Output> : RaptorJobDescription<Unit, Output> {
		override val inputSerializer get() = error("Not needed for tests")
		override val id: JobDescriptionId<Unit, Output> = JobDescriptionId("test-desc")
		override val outputSerializer get() = error("Not needed for tests")
	}

	// endregion
}
