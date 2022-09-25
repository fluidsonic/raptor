package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ExtensionTests {

	@Test
	fun testExtension() {
		raptor {
			install(CounterPlugin)

			counter {
				extensions[anyComponentExtensionKey] = "foo"
			}

			install(CounterPlugin)

			counter {
				assertEquals(expected = "foo", actual = extensions[anyComponentExtensionKey])
			}
		}
	}


	@Test
	fun testTags() {
		val raptor = raptor {
			install(NodePlugin)

			nodes {
				node("a").tags("tag a")
				node("b").tags("tag b")
				node("c") {
					tags("tag c")

					node("d").tags("tag d")
				}

				nodes.all.withTags("tag a", "tag c") {
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
			raptor[Node.rootPropertyKey]
		)
	}
}
