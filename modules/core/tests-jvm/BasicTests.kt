package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class BasicTests {

	@Test
	fun testComponentPropertyAccess() {
		val raptor = raptor {
			install(NodePlugin)

			nodes {
				node("a")
				node("b")
				node("c")

				if (name == "c")
					node("d")
			}
		}

		assertEquals(
			Node("root", listOf(
				Node("a"),
				Node("b"),
				Node("c", listOf(
					Node("d")
				))
			)),
			raptor[Node.rootPropertyKey]
		)
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {}

		assertNull(raptor[textPropertyKey])
		assertTrue(raptor.properties.isEmpty())
	}


	@Test
	fun testLazyContext() {
		lateinit var lazyContext: RaptorContext

		val raptor = raptor {
			install(object : RaptorPlugin {

				override fun RaptorPluginCompletionScope.complete() {
					propertyRegistry.register(countPropertyKey, 1)

					lazyContext = this.lazyContext

					assertEquals(
						expected = "This context cannot be used until the configuration of all components and features has completed.",
						actual = assertFails { lazyContext.properties }.message
					)
				}

				override fun RaptorPluginInstallationScope.install() {}
			})
		}

		assertSame(expected = raptor.context.properties, actual = lazyContext.properties)
	}


	@Test
	fun testTopLevelConfigurationScope() {
		val raptor = raptor {
			install(TextCollectionPlugin)

			textCollection.all {
				append("This ")
				append("is ")
			}

			install(object : RaptorPlugin {

				override fun RaptorPluginInstallationScope.install() {
					textCollection.all {
						append("working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[textPropertyKey])
	}
}
