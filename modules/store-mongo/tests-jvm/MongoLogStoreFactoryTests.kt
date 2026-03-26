package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.mongo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.bson.*
import tests.utility.*
import kotlin.test.*


class MongoLogStoreFactoryTests {

	@Test
	fun testCreate_returnsWorkingStore() = runTest {
		val database = TestMongoDatabase()
		val store = RaptorLogStoreFactory.mongo(database).create<Document>("test")

		store.append(Document("key", "value"))

		val documents = database.getCollection("test").find().toList()
		assertEquals(actual = documents.size, expected = 1)
		assertEquals(actual = documents.first()["key"], expected = "value")
	}


	@Test
	fun testCreate_differentNamesReturnIndependentStores() = runTest {
		val database = TestMongoDatabase()
		val factory = RaptorLogStoreFactory.mongo(database)
		val store1 = factory.create<Document>("store1")
		val store2 = factory.create<Document>("store2")

		store1.append(Document("key", "a"))

		val docs1 = database.getCollection("store1").find().toList()
		val docs2 = database.getCollection("store2").find().toList()
		assertEquals(actual = docs1.size, expected = 1)
		assertEquals(actual = docs1.first()["key"], expected = "a")
		assertEquals(actual = docs2, expected = emptyList())
	}


	@Test
	fun testCreate_withExplicitValueClass() = runTest {
		val database = TestMongoDatabase()
		val store = RaptorLogStoreFactory.mongo(database).create(
			name = "test",
			valueClass = Document::class,
		)

		store.append(Document("key", "value"))

		val documents = database.getCollection("test").find().toList()
		assertEquals(actual = documents.size, expected = 1)
		assertEquals(actual = documents.first()["key"], expected = "value")
	}
}
