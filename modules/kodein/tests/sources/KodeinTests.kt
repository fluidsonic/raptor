package tests

import io.fluidsonic.raptor.*
import org.junit.jupiter.api.Test
import org.kodein.di.*
import org.kodein.di.erased.*
import kotlin.test.*


class KodeinTests {

	@Test
	fun testCustomKodeinGeneration() {
		val raptor = raptor {
			install(ActivityScopedKodeinFeature)
			install(RaptorKodeinFeature)

			kodein {
				bind() from instance("bar")
			}

			activities {
				kodein {
					bind("per-activity") from instance("scoped!")
				}
			}
		}

		val dkodein = raptor.createKodein(Activity(id = "foo")).kodein.direct
		assertEquals(expected = "bar", actual = dkodein.instance())
		assertEquals(expected = "scoped!", actual = dkodein.instance("per-activity"))
		assertEquals(expected = "foo", actual = dkodein.instance<Activity>().id)
	}


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
	fun testWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorKodeinFeature to use Kodein.",
			actual = assertFails {
				raptor.kodein
			}.message
		)
	}
}
