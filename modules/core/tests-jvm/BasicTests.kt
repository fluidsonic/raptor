package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class BasicTests {

	@Test
	fun testComponentPropertyAccess() {
		val raptor = raptor {
			install(NodeFeature) {
				node("a")
				node("b")
				node("c")
			}

			install(NodeFeature) {
				nodes {
					if (name == "c")
						node("d")
				}
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
	fun testEach() {
		raptor {
			val component = this

			each {
				each {
					each {
						assertSame(expected = component, actual = this)
					}
				}
			}
		}
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
			install(object : RaptorFeature {

				override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
					propertyRegistry.register(countPropertyKey, 1)

					lazyContext = this.lazyContext

					assertEquals(
						expected = "This context cannot be used until the configuration of all components and features has completed.",
						actual = assertFails { lazyContext.properties }.message
					)
				}

				override fun RaptorFeatureScope.installed() {}
			})
		}

		assertSame(expected = raptor.context.properties, actual = lazyContext.properties)
	}


	@Test
	fun testTopLevelConfigurationScope() {
		val raptor = raptor {
			install(TextCollectionFeature)

			textCollection.all {
				append("This ")
				append("is ")
			}

			install(object : RaptorFeature {

				override fun RaptorFeatureScope.installed() {
					textCollection.all {
						append("working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[textPropertyKey])
	}
}
