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
			}
		}

		assertEquals(
			Node("root", listOf(
				Node("a", listOf(
					Node("b", listOf(
						Node("c", listOf(
							Node("d")
						))
					))
				)),
				Node("x", listOf(
					Node("y", listOf(
						Node("z")
					))
				))
			)),
			raptor[RootNodeRaptorKey]
		)
	}
}
