package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class PluginTests {

	@Test
	fun testConfigurableFeature() {
		val raptor = raptor {
			install(CounterPlugin)
			counter {
				increment()
				increment()
			}
		}

		assertEquals(expected = 2, actual = raptor[countPropertyKey])
	}


	@Test
	fun testFeature() {
		val raptor = raptor {
			install(TextCollectionPlugin)

			install(object : RaptorPlugin {

				override fun RaptorPluginInstallationScope.install() {
					textCollection.all {
						append("This is working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[textPropertyKey])
	}


	@Test
	fun testFeatureIsInstalledOnlyOnce() {
		var installCount = 0

		raptor {
			val feature = object : RaptorPlugin {

				override fun RaptorPluginInstallationScope.install() {
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
			install(CounterPlugin)
			counter {
				increment()
				increment()
			}

			install(CounterPlugin)
			counter {
				increment()
			}

			counter {
				increment()
			}
		}

		assertEquals(expected = 4, actual = raptor[countPropertyKey])
	}
}
