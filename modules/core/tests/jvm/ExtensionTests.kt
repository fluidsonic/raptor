package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ExtensionTests {

	@Test
	fun testExtension() {
		raptor {
			install(CounterFeature) {
				extensions[AnyRaptorComponentExtensionKey] = "foo"
			}

			install(CounterFeature) {
				assertEquals(expected = "foo", actual = extensions[AnyRaptorComponentExtensionKey])
			}
		}
	}


	@Test
	fun testTags() {
		val raptor = raptor {
			install(NodeFeature) {
				node("a").tags("tag a")
				node("b").tags("tag b")
				node("c") {
					tags("tag c")

					node("d").tags("tag d")
				}

				nodes.withTags("tag a", "tag c") {
					node("tagged 1")
				}

				nodes(recursive = true) {
					withTags("tag d") {
						node("tagged 2")
					}
				}
			}
		}

		assertEquals(
			Node("root", listOf(
				Node("a", listOf(
					Node("tagged 1")
				)),
				Node("b"),
				Node("c", listOf(
					Node("d", listOf(
						Node("tagged 2")
					)),
					Node("tagged 1")
				))
			)),
			raptor[RootNodeRaptorKey]
		)
	}
}
