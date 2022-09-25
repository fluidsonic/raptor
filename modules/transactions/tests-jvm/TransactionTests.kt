package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*


private val propertyKey = RaptorPropertyKey<String>("id")


@OptIn(ExperimentalCoroutinesApi::class)
class TransactionTests {

	@Test
	fun testConfigurationWithoutInstallation() {
		raptor {
			install(RaptorTransactionPlugin)

			transactions {
				onCreate {
					propertyRegistry.register(propertyKey, "ignored")
				}
			}
		}
	}


	@Test
	fun testCustomTransactionGeneration() {
		val raptor = raptor {
			install(RaptorTransactionPlugin)
			install(RequestPlugin)

			transactions {
				onCreate {
					propertyRegistry.register(propertyKey, "root")
				}
			}

			requests.all {
				transactions {
					onCreate {
						val parentId = parentContext[propertyKey]

						propertyRegistry.register(propertyKey, if (parentId != null) "request in $parentId" else "request")
					}
				}
			}
		}

		val rootTransaction = raptor.transaction()
		val requestInRootTransaction = rootTransaction.createTransaction(Request(id = "foo"))
		val requestTransaction = raptor.context.createTransaction(Request(id = "bar"))

		assertEquals(expected = "root", actual = rootTransaction[propertyKey])
		assertEquals(expected = "request in root", actual = requestInRootTransaction[propertyKey])
		assertEquals(expected = "request", actual = requestTransaction[propertyKey])
		assertEquals(expected = "foo", actual = requestInRootTransaction[Request.propertyKey]?.id)
		assertEquals(expected = "bar", actual = requestTransaction[Request.propertyKey]?.id)
		assertSame(expected = rootTransaction.context, actual = requestInRootTransaction.context.parent)
		assertSame(expected = raptor.context, actual = requestTransaction.context.parent)
	}


	@Test
	fun testInstallationWithoutConfiguration() {
		raptor {
			install(RaptorTransactionPlugin)
		}
	}


	@Test
	fun testNestedTransactionCreation() {
		val raptor = raptor {
			install(RaptorTransactionPlugin)

			transactions {
				onCreate {
					when (val context = parentContext) {
						is RaptorTransactionContext -> when (context[propertyKey]) {
							"root" -> propertyRegistry.register(propertyKey, "level 1")
							"level 1" -> Unit
							else -> error("Unexpected context")
						}

						else -> propertyRegistry.register(propertyKey, "root")
					}
				}
			}
		}

		val transaction1 = raptor.transaction()
		val transaction2 = transaction1.transaction()
		val transaction3 = transaction2.transaction()

		assertEquals(expected = "root", actual = transaction1[propertyKey])
		assertEquals(expected = "level 1", actual = transaction2[propertyKey])
		assertEquals(expected = "level 1", actual = transaction3[propertyKey])
		assertSame(expected = raptor.context, actual = transaction1.context.parent)
		assertSame(expected = transaction1.context, actual = transaction2.context.parent)
		assertSame(expected = transaction2.context, actual = transaction3.context.parent)
	}


	@Test
	fun testRaptorExtensions() = runTest {
		val raptor = raptor {
			install(RaptorTransactionPlugin)
		}

		@Suppress("USELESS_IS_CHECK")
		assertTrue(raptor.transaction() is RaptorTransaction)

		raptor.transaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}


	@Test
	fun testRaptorContextExtensions() = runTest {
		val raptor = raptor {
			install(RaptorTransactionPlugin)
		}

		@Suppress("USELESS_IS_CHECK")
		assertTrue(raptor.context.transaction() is RaptorTransaction)

		raptor.context.transaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}

	@Test
	fun testRaptorScopeExtensions() = runTest {
		val raptor = raptor {
			install(RaptorTransactionPlugin)
		}

		raptor.context.transaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}


	@Test
	fun testTransactionCreation() {
		val raptor = raptor {
			install(RaptorTransactionPlugin)

			transactions {
				var lastId = 0

				onCreate {
					propertyRegistry.register(propertyKey, (++lastId).toString())
				}
			}
		}

		val transaction1 = raptor.transaction()
		val transaction2 = raptor.transaction()

		assertEquals(expected = "1", actual = transaction1[propertyKey])
		assertEquals(expected = "2", actual = transaction2[propertyKey])
	}


	@Test
	fun testTransactionCreationWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "Plugin io.fluidsonic.raptor.transactions.RaptorTransactionPlugin is not installed.",
			actual = assertFails {
				raptor.transaction()
			}.message
		)
	}
}
