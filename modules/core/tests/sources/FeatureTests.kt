package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class FeatureTests {

	@Test
	fun testConfigurableFeature() {
		val raptor = raptor {
			install(CounterFeature) {
				increment()
				increment()
			}
		}

		assertEquals(expected = 2, actual = raptor[CountRaptorPropertyKey])
	}


	@Test
	fun testFeature() {
		val raptor = raptor {
			install(TextCollectionFeature)

			install(object : RaptorFeature {

				override fun RaptorFeatureInstallationScope.install() {
					textCollection {
						append("This is working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[TextRaptorPropertyKey])
	}


	@Test
	fun testFeatureIsInstalledOnlyOnce() {
		var installCount = 0

		raptor {
			val feature = object : RaptorFeature {

				override fun RaptorFeatureInstallationScope.install() {
					installCount += 1
				}
			}

			install(feature)
			install(feature)
			install(feature)
		}

		assertEquals(expected = 1, actual = installCount)
	}


	@Test
	fun testMultipleFeatureConfigurations() {
		val raptor = raptor {
			install(CounterFeature) {
				increment()
				increment()
			}

			install(CounterFeature) {
				increment()
			}

			counter {
				increment()
			}
		}

		assertEquals(expected = 4, actual = raptor[CountRaptorPropertyKey])
	}
}
