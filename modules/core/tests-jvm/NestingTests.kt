package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class NestingTests {

	@Test
	fun testNesting() {
		val raptor = raptor {
			install(NodeFeature) {
				val cNode = node("a").node("b").node("c")
				cNode.node("d")

				node("x") {
					node("y") {
						node("z")
					}
				}

				nodes {
					if (name == "x")
						node("x2")
				}

				nodes(recursive = false) {
					if (name == "c")
						node("c2")
				}

				nodes(recursive = true) {
					if (name == "c")
						node("c3")
				}
			}
		}

		assertEquals(
			Node("root", listOf(
				Node("a", listOf(
					Node("b", listOf(
						Node("c", listOf(
							Node("d"),
							Node("c3")
						))
					))
				)),
				Node("x", listOf(
					Node("y", listOf(
						Node("z")
					)),
					Node("x2")
				))
			)),
			raptor[Node.rootPropertyKey]
		)
	}
}
