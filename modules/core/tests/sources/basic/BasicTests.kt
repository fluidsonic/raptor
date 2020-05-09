package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class BasicTests {

	@Test
	fun testConfigurableFeature() {
		val raptor = raptor {
			install(BasicConfigurableFeature) {
				append("This ")
				append("is ")
				append("working!")
			}
		}

		assertEquals(expected = "This is working!", actual = raptor[BasicRaptorKey])
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {}

		assertNull(actual = raptor[BasicRaptorKey])
		assertTrue(raptor is DefaultRaptor)
		// FIXME use Raptor.toString() instead?
		assertTrue(raptor.properties.isEmpty())
	}


	@Test
	fun testFeature() {
		val raptor = raptor {
			install(BasicFeature)

			install(object : RaptorFeature {

				override fun RaptorFeatureInstallationScope.install() {
					registry.all<BasicComponent> {
						append("This ")
						append("is ")
					}
					registry.all<BasicComponent> {
						append("working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[BasicRaptorKey])
	}
}
