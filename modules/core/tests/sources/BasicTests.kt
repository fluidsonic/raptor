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

			install(object : RaptorFeature {

				override fun RaptorFeatureInstallationScope.install() {
					registry.all<NodeComponent> {
						if (name == "c")
							node("d")
					}
				}
			})
		}

		assertEquals(
			Node(name = "root", children = listOf(
				Node(name = "a"),
				Node(name = "b"),
				Node(name = "c", children = listOf(
					Node(name = "d")
				))
			)),
			raptor[RootNodeRaptorKey]
		)
	}


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
	fun testForEach() {
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
	fun testMultipleFeatureConfiguartions() {
		val raptor = raptor {
			install(BasicConfigurableFeature) {
				append("This ")
				append("is ")
			}
			install(BasicConfigurableFeature) {
				append("working!")
			}
		}

		assertEquals(expected = "This is working!", actual = raptor[BasicRaptorKey])
	}
}
