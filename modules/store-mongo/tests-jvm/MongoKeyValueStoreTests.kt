package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.RaptorKeyValueStore.*
import io.fluidsonic.raptor.store.mongo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import tests.utility.*
import kotlin.test.*


class MongoKeyValueStoreTests {

	private fun createStore(): RaptorKeyValueStore<String, String> {
		val database = TestMongoDatabase()
		return RaptorKeyValueStoreFactory.mongo(database).create<String, String>("test")
	}

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
	fun testEntries_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.entries().toList(), expected = emptyList())
	}

	@Test
	fun testEntries_returnsAllPairs() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.entries().toSet(), expected = setOf("a" to "1", "b" to "2"))
	}

	@Test
	fun testKeys_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.keys().toList(), expected = emptyList())
	}

	@Test
	fun testKeys_returnsAllKeys() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.keys().toSet(), expected = setOf("a", "b"))
	}

	@Test
	fun testValues_emptyStore_returnsEmptyFlow() = runTest {
		val store = createStore()
		assertEquals(actual = store.values().toList(), expected = emptyList())
	}

	@Test
	fun testValues_returnsAllValues() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.set("b", "2")
		assertEquals(actual = store.values().toSet(), expected = setOf("1", "2"))
	}

	@Test
	fun testUpdate_update_replacesExistingValue() = runTest {
		val store = createStore()
		store.set("a", "1")
		val result = store.update("a") { UpdateDecision.Update("2") }
		assertEquals(actual = result, expected = "2")
		assertEquals(actual = store.get("a"), expected = "2")
	}

	@Test
	fun testUpdate_update_insertsWhenAbsent() = runTest {
		val store = createStore()
		val result = store.update("a") { UpdateDecision.Update("1") }
		assertEquals(actual = result, expected = "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testUpdate_receivesCurrentValue() = runTest {
		val store = createStore()
		store.set("a", "1")
		store.update("a") { current ->
			assertEquals(actual = current, expected = "1")

			UpdateDecision.Keep
		}
	}

	@Test
	fun testUpdate_keep_leavesValueUnchangedAndReturnsIt() = runTest {
		val store = createStore()
		store.set("a", "1")
		val result = store.update("a") { UpdateDecision.Keep }
		assertEquals(actual = result, expected = "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testUpdate_keep_absentKey_returnsNull() = runTest {
		val store = createStore()
		assertNull(store.update("a") { UpdateDecision.Keep })
		assertNull(store.get("a"))
	}

	@Test
	fun testUpdate_remove_deletesEntry() = runTest {
		val store = createStore()
		store.set("a", "1")
		assertNull(store.update("a") { UpdateDecision.Remove })
		assertNull(store.get("a"))
	}

	@Test
	fun testUpdate_retriesOnConflict() = runTest {
		val database = TestMongoDatabase()
		val store = RaptorKeyValueStoreFactory.mongo(database).create<String, String>("test")
		val otherHandle = RaptorKeyValueStoreFactory.mongo(database).create<String, String>("test")
		store.set("a", "1")

		var attempts = 0
		val result = store.update("a") {
			attempts += 1

			// A second handle changes the stored document after we read it, but only once. The
			// raw-document filter then rejects our stale write (matchedCount == 0), forcing a re-read.
			// decide is non-suspending, so the concurrent write is driven via runBlocking.
			if (attempts == 1)
				runBlocking { otherHandle.set("a", "x") }

			UpdateDecision.Update("2")
		}

		assertEquals(actual = attempts, expected = 2)
		assertEquals(actual = result, expected = "2")
		assertEquals(actual = store.get("a"), expected = "2")
	}

	@Test
	fun testUpdate_exhaustedAttempts_throws() = runTest {
		val database = TestMongoDatabase()
		val store = RaptorKeyValueStoreFactory.mongo(database).create<String, String>("test")
		val otherHandle = RaptorKeyValueStoreFactory.mongo(database).create<String, String>("test")
		store.set("a", "1")

		var attempts = 0
		assertFailsWith<RaptorOptimisticUpdateException> {
			store.update("a", maxAttempts = 3) {
				attempts += 1

				// A second handle keeps changing the stored document, forcing a perpetual conflict.
				// decide is non-suspending, so the concurrent write is driven via runBlocking.
				runBlocking { otherHandle.set("a", "v$attempts") }

				UpdateDecision.Update("new")
			}
		}

		assertEquals(actual = attempts, expected = 3)
	}
}
