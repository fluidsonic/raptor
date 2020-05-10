package tests

import io.fluidsonic.raptor.*
import org.junit.jupiter.api.Test
import kotlin.test.*


class TransactionTests {

	@Test
	fun testTransactionCreation() {
		val raptor = raptor {
			install(RaptorTransactionFeature)

			transactions {

			}
		}

		val transaction = raptor.context.createTransaction()
	}


	@Test
	fun testContextAccessWithoutInstallationFails() {
		val raptor = raptor {}

		assertFails {
			raptor.context
		}
	}
}
