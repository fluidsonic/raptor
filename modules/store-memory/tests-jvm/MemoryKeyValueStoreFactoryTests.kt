package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.memory.*
import kotlinx.coroutines.test.*
import kotlin.test.*


class MemoryKeyValueStoreFactoryTests {

	@Test
	fun testCreate_returnsWorkingStore() = runTest {
		val store = RaptorKeyValueStoreFactory.memory().create(
			name = "test",
			keyClass = String::class,
			valueClass = String::class,
		)
		store.set("a", "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testMemory_returnsSingletonFactory() {
		assertSame(actual = RaptorKeyValueStoreFactory.memory(), expected = RaptorKeyValueStoreFactory.memory())
	}

	@Test
	fun testCreate_returnsDifferentStoreInstances() = runTest {
		val factory = RaptorKeyValueStoreFactory.memory()
		val store1 = factory.create<String, String>("store1")
		val store2 = factory.create<String, String>("store2")
		store1.set("a", "1")
		assertNull(store2.get("a"))
	}

	@Test
	fun testCreate_reifiedExtension() = runTest {
		val store = RaptorKeyValueStoreFactory.memory().create<String, Int>("test")
		store.set("a", 42)
		assertEquals(actual = store.get("a"), expected = 42)
	}
}
