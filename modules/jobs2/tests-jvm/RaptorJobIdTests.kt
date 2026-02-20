package io.fluidsonic.raptor.jobs2

import kotlin.test.*


class RaptorJobIdTests {

	@Test
	fun `toString returns the value`() {
		val id = RaptorJobId<Unit, Unit>("my-job")

		assertEquals(actual = id.toString(), expected = "my-job")
	}


	@Test
	fun `discriminator returns RaptorJobId`() {
		val id = RaptorJobId<Unit, Unit>("my-job")

		assertEquals(actual = id.discriminator, expected = "RaptorJobId")
	}


	@Test
	fun `equality is value-based`() {
		val a = RaptorJobId<Unit, Unit>("same")
		val b = RaptorJobId<Unit, Unit>("same")
		val c = RaptorJobId<Unit, Unit>("different")

		assertEquals(actual = a, expected = b)
		assertNotEquals(illegal = c, actual = a)
	}


	@Test
	fun `JobDescriptionId toString returns the value`() {
		val id = JobDescriptionId<Unit, Unit>("desc-1")

		assertEquals(actual = id.toString(), expected = "desc-1")
	}


	@Test
	fun `JobDescriptionId equality is value-based`() {
		val a = JobDescriptionId<Unit, Unit>("same")
		val b = JobDescriptionId<Unit, Unit>("same")
		val c = JobDescriptionId<Unit, Unit>("different")

		assertEquals(actual = a, expected = b)
		assertNotEquals(illegal = c, actual = a)
	}


	@Test
	fun `JobExecutionId toString returns the value`() {
		val id = JobExecutionId("exec-1")

		assertEquals(actual = id.toString(), expected = "exec-1")
	}


	@Test
	fun `JobExecutionId equality is value-based`() {
		val a = JobExecutionId("same")
		val b = JobExecutionId("same")
		val c = JobExecutionId("different")

		assertEquals(actual = a, expected = b)
		assertNotEquals(illegal = c, actual = a)
	}
}
