package io.fluidsonic.raptor.service2

import kotlin.test.*


/**
 * Tests for composite input sources (AllInputSource, AnyInputSource).
 */
class CompositeInputSourceTests {

	@Test
	fun `AllInputSource stores all sources`() {
		val source1 = StartInputSource
		val source2 = StartInputSource // Same object, will be deduplicated in set

		val all = AllInputSource(setOf(source1, source2))

		assertEquals(actual = all.sources.size, expected = 1) // Deduplicated
		assertTrue(source1 in all.sources)
	}


	@Test
	fun `AllInputSource with multiple different sources`() {
		val source1 = StartInputSource
		val source2 = DefaultAggregatesLoadedInputSource

		val all = AllInputSource(setOf(source1, source2))

		assertEquals(actual = all.sources.size, expected = 2)
		assertTrue(source1 in all.sources)
		assertTrue(source2 in all.sources)
	}


	@Test
	fun `AllInputSource with empty set`() {
		val all = AllInputSource<RaptorService2, Unit>(emptySet())

		assertTrue(all.sources.isEmpty())
	}


	@Test
	fun `AnyInputSource stores all sources`() {
		val source1 = StartInputSource
		val source2 = DefaultAggregatesLoadedInputSource

		val any = AnyInputSource(setOf(source1, source2))

		assertEquals(actual = any.sources.size, expected = 2)
		assertTrue(source1 in any.sources)
		assertTrue(source2 in any.sources)
	}


	@Test
	fun `AnyInputSource with empty set`() {
		val any = AnyInputSource<RaptorService2, Unit>(emptySet())

		assertTrue(any.sources.isEmpty())
	}


	@Test
	fun `AllInputSource equality`() {
		val sources = setOf(StartInputSource, DefaultAggregatesLoadedInputSource)

		val all1 = AllInputSource(sources)
		val all2 = AllInputSource(sources)

		assertEquals(all1, all2)
		assertEquals(all1.hashCode(), all2.hashCode())
	}


	@Test
	fun `AnyInputSource equality`() {
		val sources = setOf(StartInputSource, DefaultAggregatesLoadedInputSource)

		val any1 = AnyInputSource(sources)
		val any2 = AnyInputSource(sources)

		assertEquals(any1, any2)
		assertEquals(any1.hashCode(), any2.hashCode())
	}


	@Test
	fun `AllInputSource and AnyInputSource are not equal with same sources`() {
		val sources = setOf<RaptorServiceInput2<RaptorService2, out Unit>>(StartInputSource)

		val all = AllInputSource(sources)
		val any = AnyInputSource(sources)

		assertNotEquals<Any>(all, any)
	}
}
