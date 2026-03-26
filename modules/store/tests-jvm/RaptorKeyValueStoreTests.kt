package tests

import io.fluidsonic.raptor.store.*
import kotlinx.coroutines.test.*
import kotlin.test.*


class RaptorKeyValueStoreTests {

	@Test
	fun testSetOrRemove_nonNullValue_callsSet() = runTest {
		val store = SpyKeyValueStore()
		store.setOrRemove("k", "v")
		assertEquals(actual = store.setCalls, expected = listOf("k" to "v"))
		assertEquals(actual = store.removeCalls, expected = emptyList())
	}

	@Test
	fun testSetOrRemove_nullValue_callsRemove() = runTest {
		val store = SpyKeyValueStore()
		store.setOrRemove("k", null)
		assertEquals(actual = store.removeCalls, expected = listOf("k"))
		assertEquals(actual = store.setCalls, expected = emptyList())
	}

	@Test
	fun testSetIfAbsentOrRemove_nonNullValue_callsSetIfAbsent() = runTest {
		val store = SpyKeyValueStore()
		store.setIfAbsentOrRemove("k", "v")
		assertEquals(actual = store.setIfAbsentCalls, expected = listOf("k" to "v"))
		assertEquals(actual = store.removeCalls, expected = emptyList())
	}

	@Test
	fun testSetIfAbsentOrRemove_nullValue_callsRemove() = runTest {
		val store = SpyKeyValueStore()
		store.setIfAbsentOrRemove("k", null)
		assertEquals(actual = store.removeCalls, expected = listOf("k"))
		assertEquals(actual = store.setIfAbsentCalls, expected = emptyList())
	}
}
