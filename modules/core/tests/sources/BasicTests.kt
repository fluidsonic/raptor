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

		assertNull(actual = raptor[TextRaptorPropertyKey])
		assertEquals(
			expected = "[raptor] -> (empty)",
			actual = raptor.toString()
		)
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
