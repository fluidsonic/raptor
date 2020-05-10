package tests

import io.fluidsonic.raptor.*
import org.junit.jupiter.api.Test
import org.kodein.di.*
import org.kodein.di.erased.*
import kotlin.test.*


class KodeinTests {

	@Test
	fun testKodein() {
		val raptor = raptor {
			install(RaptorKodeinFeature)

			kodein {
				bind() from instance("bar")
			}
		}

		assertEquals(expected = "bar", actual = raptor.kodein.direct.instance())
	}


	@Test
	fun testKodeinForTransaction() {
		val raptor = raptor {
			install(RaptorKodeinFeature)
			install(RaptorTransactionFeature)

			kodein {
				bind() from instance("bar")
			}

			transactions {
				kodein {
					bind() from instance(1)
				}
			}
		}

		val dkodein = raptor.createTransaction().kodein.direct
		assertEquals(expected = "bar", actual = dkodein.instance())
		assertEquals(expected = 1, actual = dkodein.instance())
	}


	@Test
	fun testKodeinWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorKodeinFeature in order to use Kodein.",
			actual = assertFails {
				raptor.kodein
			}.message
		)
	}
}
