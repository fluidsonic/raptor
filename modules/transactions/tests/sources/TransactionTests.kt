package tests

import io.fluidsonic.raptor.*
import org.junit.jupiter.api.Test
import tests.utility.*
import kotlin.test.*


class TransactionTests {

	@Test
	fun testConfigurationWithoutInstallation() {
		raptor {
			transactions {
				onCreate {
					propertyRegistry.register(IdRaptorPropertyKey, "ignored")
				}
			}
		}
	}


	@Test
	fun testCustomTransactionGeneration() {
		val raptor = raptor {
			install(RaptorTransactionFeature)
			install(RequestFeature)

			transactions {
				onCreate {
					propertyRegistry.register(IdRaptorPropertyKey, "root")
				}
			}

			requests {
				transactions {
					onCreate {
						val parentId = context[IdRaptorPropertyKey]

						propertyRegistry.register(IdRaptorPropertyKey, if (parentId != null) "request in $parentId" else "request")
					}
				}
			}
		}

		val rootTransaction = raptor.createTransaction()
		val requestInRootTransaction = rootTransaction.createTransaction(Request(id = "foo"))
		val requestTransaction = raptor.context.createTransaction(Request(id = "bar"))

		assertEquals(expected = "root", actual = rootTransaction[IdRaptorPropertyKey])
		assertEquals(expected = "request in root", actual = requestInRootTransaction[IdRaptorPropertyKey])
		assertEquals(expected = "request", actual = requestTransaction[IdRaptorPropertyKey])
		assertEquals(expected = "foo", actual = requestInRootTransaction[RequestRaptorPropertyKey]?.id)
		assertEquals(expected = "bar", actual = requestTransaction[RequestRaptorPropertyKey]?.id)
		assertSame(expected = rootTransaction.context, actual = requestInRootTransaction.context.parent)
		assertSame(expected = raptor.context, actual = requestTransaction.context.parent)
	}


	@Test
	fun testInstallationWithoutConfiguration() {
		raptor {
			install(RaptorTransactionFeature)
		}
	}


	@Test
	fun testNestedTransactionCreation() {
		val raptor = raptor {
			install(RaptorTransactionFeature)

			transactions {
				onCreate {
					when (val context = context) {
						is RaptorTransactionContext -> when (context[IdRaptorPropertyKey]) {
							"root" -> propertyRegistry.register(IdRaptorPropertyKey, "level 1")
							"level 1" -> Unit
							else -> error("Unexpected context")
						}
						else -> propertyRegistry.register(IdRaptorPropertyKey, "root")
					}
				}
			}
		}

		val transaction1 = raptor.createTransaction()
		val transaction2 = transaction1.createTransaction()
		val transaction3 = transaction2.createTransaction()

		assertEquals(expected = "root", actual = transaction1[IdRaptorPropertyKey])
		assertEquals(expected = "level 1", actual = transaction2[IdRaptorPropertyKey])
		assertEquals(expected = "level 1", actual = transaction3[IdRaptorPropertyKey])
		assertSame(expected = raptor.context, actual = transaction1.context.parent)
		assertSame(expected = transaction1.context, actual = transaction2.context.parent)
		assertSame(expected = transaction2.context, actual = transaction3.context.parent)
	}


	@Test
	fun testRaptorExtensions() {
		val raptor = raptor {
			install(RaptorTransactionFeature)
		}

		@Suppress("USELESS_IS_CHECK")
		assertTrue(raptor.createTransaction() is RaptorTransaction)

		raptor.withNewTransaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}


	@Test
	fun testRaptorContextExtensions() {
		val raptor = raptor {
			install(RaptorTransactionFeature)
		}

		@Suppress("USELESS_IS_CHECK")
		assertTrue(raptor.context.createTransaction() is RaptorTransaction)

		raptor.context.withNewTransaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}

	@Test
	fun testRaptorScopeExtensions() {
		val raptor = raptor {
			install(RaptorTransactionFeature)
		}

		raptor.context.asScope().withNewTransaction {
			@Suppress("USELESS_IS_CHECK")
			assertTrue(this is RaptorTransactionScope)
		}
	}


	@Test
	fun testTransactionCreation() {
		val raptor = raptor {
			install(RaptorTransactionFeature)

			transactions {
				var lastId = 0

				onCreate {
					propertyRegistry.register(IdRaptorPropertyKey, (++lastId).toString())
				}
			}
		}

		val transaction1 = raptor.createTransaction()
		val transaction2 = raptor.createTransaction()

		assertEquals(expected = "1", actual = transaction1[IdRaptorPropertyKey])
		assertEquals(expected = "2", actual = transaction2[IdRaptorPropertyKey])
	}


	@Test
	fun testTransactionCreationWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorTransactionFeature to use transactions.",
			actual = assertFails {
				raptor.createTransaction()
			}.message
		)
	}
}
