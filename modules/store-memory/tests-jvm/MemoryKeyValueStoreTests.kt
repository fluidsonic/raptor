package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.memory.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*


class MemoryKeyValueStoreTests {

	private fun createStore(): RaptorKeyValueStore<String, String> =
		RaptorKeyValueStoreFactory.memory().create<String, String>("test")

	@Test
	fun testGet_missingKey_returnsNull() = runTest {
		val store = createStore()
		assertNull(store.get("x"))
	}

	@Test
	fun testGet_existingKey_returnsValue() = runTest {
		val store = createStore()
		store.set("a", "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testSet_newKey() = runTest {
		val store = createStore()
		store.set("a", "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testSet_overwritesExistingValue() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("a", "2")
		assertEquals(actual = store.get("a"), expected = "2")
	}

	@Test
	fun testRemove_existingKey_returnsTrue() = runTest {
		val store = createStore()
		store.set("a", "1")
		assertTrue(store.remove("a"))
		assertNull(store.get("a"))
	}

	@Test
	fun testRemove_missingKey_returnsFalse() = runTest {
		val store = createStore()
		assertFalse(store.remove("x"))
	}

	@Test
	fun testSetIfAbsent_absentKey_returnsTrueAndStoresValue() = runTest {
		val store = createStore()
		assertTrue(store.setIfAbsent("a", "1"))
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testSetIfAbsent_presentKey_returnsFalseAndKeepsOriginal() = runTest {
		val store = createStore()
		store.set("a", "1")
		assertFalse(store.setIfAbsent("a", "2"))
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testClear_removesAllEntries() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		store.clear()
		assertNull(store.get("a"))
		assertNull(store.get("b"))
	}

	@Test
	fun testClear_emptyStore_doesNotThrow() = runTest {
		val store = createStore()
		store.clear()
	}

	@Test
	fun testEntries_returnsAllPairs() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.entries().toSet(), expected = setOf("a" to "1", "b" to "2"))
	}

	@Test
	fun testEntries_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.entries().toList(), expected = emptyList())
	}

	@Test
	fun testKeys_returnsAllKeys() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.keys().toSet(), expected = setOf("a", "b"))
	}

	@Test
	fun testKeys_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.keys().toList(), expected = emptyList())
	}

	@Test
	fun testValues_returnsAllValues() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.values().toSet(), expected = setOf("1", "2"))
	}

	@Test
	fun testValues_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.values().toList(), expected = emptyList())
	}
}
