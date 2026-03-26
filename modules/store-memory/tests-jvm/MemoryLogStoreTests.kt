package tests

import io.fluidsonic.raptor.store.*
import io.fluidsonic.raptor.store.memory.*
import kotlin.test.*
import kotlinx.coroutines.test.*


class MemoryLogStoreTests {

	@Test
	fun testAppend_singleValue() = runTest {
		val store = createStore<String>()

		store.append("hello")

		assertEquals(actual = store.values(), expected = listOf("hello"))
	}


	@Test
	fun testAppend_multipleValues() = runTest {
		val store = createStore<String>()

		store.append("a")
		store.append("b")
		store.append("c")

		assertEquals(actual = store.values(), expected = listOf("a", "b", "c"))
	}


	@Test
	fun testAppend_preservesOrder() = runTest {
		val store = createStore<Int>()

		for (i in 1..10)
			store.append(i)

		assertEquals(actual = store.values(), expected = (1..10).toList())
	}


	@Test
	fun testAppend_duplicateValues_preservesAll() = runTest {
		val store = createStore<String>()

		store.append("dup")
		store.append("dup")
		store.append("dup")

		assertEquals(actual = store.values(), expected = listOf("dup", "dup", "dup"))
	}


	@Test
	fun testAppend_dataClassValues() = runTest {
		data class Event(val id: Int, val name: String)

		val store = createStore<Event>()

		store.append(Event(id = 1, name = "first"))
		store.append(Event(id = 2, name = "second"))

		assertEquals(
			actual = store.values(),
			expected = listOf(Event(id = 1, name = "first"), Event(id = 2, name = "second")),
		)
	}


	@Test
	fun testAppend_contravariance() = runTest {
		val store = createStore<Any>()

		store.append("string")
		store.append(42)

		assertEquals(actual = store.values(), expected = listOf("string", 42))
	}


	@Test
	fun testValues_emptyStore_returnsEmptyList() {
		val store = createStore<String>()

		assertEquals(actual = store.values(), expected = emptyList())
	}


	@Test
	fun testValues_differentStores_areIndependent() = runTest {
		val store1 = createStore<String>()
		val store2 = createStore<String>()

		store1.append("only-in-1")
		store2.append("only-in-2")

		assertEquals(actual = store1.values(), expected = listOf("only-in-1"))
		assertEquals(actual = store2.values(), expected = listOf("only-in-2"))
	}


	@Test
	fun testFactory_createsWorkingStore() = runTest {
		val factory = RaptorLogStoreFactory.memory()
		val store = factory.create(name = "test", valueClass = String::class)

		store.append("hello")

		assertIs<MemoryLogStore<String>>(store)
		assertEquals(actual = store.values(), expected = listOf("hello"))
	}


	@Test
	fun testFactory_createsDifferentStoreInstances() = runTest {
		val factory = RaptorLogStoreFactory.memory()
		val store1 = factory.create(name = "store1", valueClass = String::class) as MemoryLogStore<String>
		val store2 = factory.create(name = "store2", valueClass = String::class) as MemoryLogStore<String>

		store1.append("only-in-1")
		store2.append("only-in-2")

		assertEquals(actual = store1.values(), expected = listOf("only-in-1"))
		assertEquals(actual = store2.values(), expected = listOf("only-in-2"))
	}


	@Test
	fun testFactory_createWithReifiedType() {
		val factory = RaptorLogStoreFactory.memory()
		val store = factory.create<String>(name = "test")

		assertIs<MemoryLogStore<String>>(store)
	}


	@Test
	fun testFactory_returnsSingletonFactory() {
		assertSame(actual = RaptorLogStoreFactory.memory(), expected = RaptorLogStoreFactory.memory())
	}
}


private fun <Value : Any> createStore(): MemoryLogStore<Value> = MemoryLogStore()
