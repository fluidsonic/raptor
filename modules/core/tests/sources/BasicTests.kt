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
			raptor[RootNodeRaptorKey]
		)
	}


	@Test
	fun testConfigure() {
		raptor {
			val component = this

			configure {
				configure {
					configure {
						assertSame(expected = component, actual = this)
					}
				}
			}
		}
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {}

		assertNull(raptor[TextRaptorPropertyKey])
		assertTrue(raptor.properties.isEmpty())
	}


	@Test
	fun testFinalizationCompletion() {
		raptor {
			install(object : RaptorFeature {

				override fun RaptorFeatureFinalizationScope.finalize() {
					propertyRegistry.register(CountRaptorPropertyKey, 1)

					onCompleted {
						assertEquals(expected = "bar", actual = context[TextRaptorPropertyKey])
					}
				}


				override fun RaptorFeatureInstallationScope.install() = Unit
			})

			install(object : RaptorFeature {

				override fun RaptorFeatureFinalizationScope.finalize() {
					propertyRegistry.register(TextRaptorPropertyKey, "bar")

					onCompleted {
						assertEquals(expected = 1, actual = context[CountRaptorPropertyKey])
					}
				}


				override fun RaptorFeatureInstallationScope.install() = Unit
			})
		}
	}


	@Test
	fun testGlobalConfigurationScope() {
		val raptor = raptor {
			install(TextCollectionFeature)

			textCollection {
				append("This ")
				append("is ")
			}

			install(object : RaptorFeature {

				override fun RaptorFeatureInstallationScope.install() {
					textCollection {
						append("working!")
					}
				}
			})
		}

		assertEquals(expected = "This is working!", actual = raptor[TextRaptorPropertyKey])
	}
}
