package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class PluginTests {

	@Test
	fun testPlugin() {
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
	fun testPluginIsInstalledOnlyOnce() {
		var installCount = 0

		raptor {
			val plugin = object : RaptorPlugin {

				override fun RaptorPluginInstallationScope.install() {
					installCount += 1
				}
			}

			install(plugin)
			install(plugin)
			install(plugin)
		}

		assertEquals(expected = 1, actual = installCount)
	}


	@Test
	fun testMultiplePluginConfigurations() {
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
