package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class RecursionTests {

	@Test
	fun testRecursion() {
		val raptor = raptor {
			install(NodeFeature) {
				val abc = node("a").node("b").node("c")
				abc.node("d")

				node("x").invoke { // 😭 FIXME
					node("y").invoke { // 😭 FIXME
						node("z")
					}
				}
			}
		}

		assertEquals(
			Node(name = "root", children = listOf(
				Node(name = "a", children = listOf(
					Node(name = "b", children = listOf(
						Node(name = "c", children = listOf(
							Node(name = "d")
						))
					))
				)),
				Node(name = "x", children = listOf(
					Node(name = "y", children = listOf(
						Node(name = "z")
					))
				))
			)),
			raptor[RootNodeRaptorKey]
		)
	}
}
