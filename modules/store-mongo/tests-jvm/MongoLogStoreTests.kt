package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.mongo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.bson.*
import tests.utility.*
import kotlin.test.*


class MongoLogStoreTests {

	private lateinit var database: TestMongoDatabase


	@BeforeTest
	fun setUp() {
		database = TestMongoDatabase()
	}


	private fun createStore(name: String = "test"): RaptorLogStore<Document> =
		RaptorLogStoreFactory.mongo(database).create<Document>(name)


	private suspend fun findAll(name: String = "test"): List<Document> =
		database.getCollection(name).find().toList()


	@Test
	fun testAppend_singleValue() = runTest {
		val store = createStore()

		store.append(Document("key", "value"))

		val documents = findAll()
		assertEquals(actual = documents.size, expected = 1)
		assertEquals(actual = documents.first()["key"], expected = "value")
	}


	@Test
	fun testAppend_multipleValues() = runTest {
		val store = createStore()

		store.append(Document("key", "first"))
		store.append(Document("key", "second"))
		store.append(Document("key", "third"))

		val documents = findAll()
		assertEquals(actual = documents.size, expected = 3)
		assertEquals(
			actual = documents.map { it["key"] },
			expected = listOf("first", "second", "third"),
		)
	}


	@Test
	fun testAppend_duplicateValues_storesAll() = runTest {
		val store = createStore()

		store.append(Document("key", "same"))
		store.append(Document("key", "same"))

		val documents = findAll()
		assertEquals(actual = documents.size, expected = 2)
		assertTrue(documents.all { it["key"] == "same" })
	}


	@Test
	fun testAppend_emptyCollectionInitially() = runTest {
		createStore()

		assertEquals(actual = findAll(), expected = emptyList())
	}
}
