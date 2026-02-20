package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*
import kotlin.test.*


class RaptorJobUpdateTests {

	private val t0 = Timestamp.fromEpochMilliseconds(0)
	private val t1 = Timestamp.fromEpochMilliseconds(1000)


	@Test
	fun `update with change`() {
		val job = testJob()
		val change = RaptorJobChange.ExecutionStarted(timestamp = t1)
		val update = RaptorJobUpdate(job = job, change = change)

		assertSame(actual = update.job, expected = job)
		assertSame(actual = update.change, expected = change)
	}


	@Test
	fun `update with null change for initial snapshot`() {
		val job = testJob()
		val update = RaptorJobUpdate(job = job, change = null)

		assertSame(actual = update.job, expected = job)
		assertNull(update.change)
	}


	@Test
	fun `equality is structural`() {
		val job = testJob()
		val change = RaptorJobChange.ExecutionStarted(timestamp = t1)
		val a = RaptorJobUpdate(job = job, change = change)
		val b = RaptorJobUpdate(job = job, change = change)

		assertEquals(actual = a, expected = b)
	}


	private fun testJob(): RaptorJob<Unit, Nothing> = TestJob(
		cancellationRequestTimestamp = null,
		executions = emptyList(),
	)


	private data class TestJob(
		override val cancellationRequestTimestamp: Timestamp?,
		override val creationTimestamp: Timestamp = Timestamp.fromEpochMilliseconds(0),
		override val description: RaptorJobDescription<Unit, Nothing> = TestDescription(),
		override val executions: List<RaptorJobExecution<Nothing>>,
		override val id: RaptorJobId<Unit, Nothing> = RaptorJobId("test-job"),
		override val input: Unit = Unit,
	) : RaptorJob<Unit, Nothing>


	private class TestDescription : RaptorJobDescription<Unit, Nothing> {
		override val inputSerializer get() = error("Not needed for tests")
		override val id = JobDescriptionId<Unit, Nothing>("test")
		override val outputSerializer get() = error("Not needed for tests")
	}
}
