package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class NestingTests {

	@Test
	fun testNesting() {
		val raptor = raptor {
			install(NodePlugin)
			nodes {
				val cNode = node("a").node("b").node("c")
				cNode.node("d")

				node("x") {
					node("y") {
						node("z")
					}
				}

				all {
					if (name == "x")
						node("x2")
				}

				all(recursive = false) {
					if (name == "c")
						node("c2")
				}

				all(recursive = true) {
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
