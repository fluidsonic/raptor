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
					propertyRegistry.register(IdRaptorTransactionPropertyKey, "ignored")
				}
			}
		}
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
						is RaptorTransactionContext -> when (context[IdRaptorTransactionPropertyKey]) {
							"root" -> propertyRegistry.register(IdRaptorTransactionPropertyKey, "level 1")
							"level 1" -> Unit
							else -> error("Unexpected context")
						}
						else -> propertyRegistry.register(IdRaptorTransactionPropertyKey, "root")
					}
				}
			}
		}

		val transaction1 = raptor.createTransaction()
		val transaction2 = transaction1.createTransaction()
		val transaction3 = transaction2.createTransaction()

		assertEquals(expected = "root", actual = transaction1[IdRaptorTransactionPropertyKey])
		assertEquals(expected = "level 1", actual = transaction2[IdRaptorTransactionPropertyKey])
		assertEquals(expected = "level 1", actual = transaction3[IdRaptorTransactionPropertyKey])
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
					propertyRegistry.register(IdRaptorTransactionPropertyKey, (++lastId).toString())
				}
			}
		}

		val transaction1 = raptor.createTransaction()
		val transaction2 = raptor.createTransaction()

		assertEquals(expected = "1", actual = transaction1[IdRaptorTransactionPropertyKey])
		assertEquals(expected = "2", actual = transaction2[IdRaptorTransactionPropertyKey])
	}


	@Test
	fun testTransactionCreationWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorTransactionFeature in order to use transactions.",
			actual = assertFails {
				raptor.createTransaction()
			}.message
		)
	}
}
