package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.mongo.*
import kotlinx.coroutines.test.*
import tests.utility.*
import kotlin.test.*


class MongoKeyValueStoreFactoryTests {

	@Test
	fun testCreate_returnsWorkingStore() = runTest {
		val database = TestMongoDatabase()
		val store = RaptorKeyValueStoreFactory.mongo(database).create(
			name = "test",
			keyClass = String::class,
			valueClass = String::class,
		)
		store.set("a", "1")
		assertEquals(actual = store.get("a"), expected = "1")
	}

	@Test
	fun testCreate_differentNamesReturnIndependentStores() = runTest {
		val database = TestMongoDatabase()
		val factory = RaptorKeyValueStoreFactory.mongo(database)
		val store1 = factory.create<String, String>("store1")
		val store2 = factory.create<String, String>("store2")
		store1.set("a", "1")
		assertNull(store2.get("a"))
	}
}
