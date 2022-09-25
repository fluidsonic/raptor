package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class DebuggingTests {

	@Test
	fun testEmptyRaptorToString() {
		val raptor = raptor {}

		assertEquals(
			expected = """
				[raptor] ->
					[context] -> (empty)
			""".trimIndent(),
			actual = raptor.toString()
		)
	}


	@Test
	fun testEmptyRegistriesToString() {
		assertEquals(
			expected = "[component registry] -> (empty)",
			actual = DefaultComponentRegistry()
				.also { it.all(DummyComponent.key) }
				.toString()
		)

		assertEquals(
			expected = "[property registry] -> (empty)",
			actual = DefaultPropertyRegistry().toString()
		)
	}


	@Test
	fun testRegistriesToString() {
		raptor {
			install(TextCollectionPlugin)
			textCollection.all {
				append("foo")
			}

			install(CounterPlugin)
			counter {
				increment()

				extensions[anyComponentExtensionKey] = "counter extension"
			}

			install(NodePlugin)
			nodes {
				node("a") {
					node("a1")
					node("a2")

					extensions[anyComponentExtensionKey] = "node extension"
				}
				node("b")
				node("c")
			}

			require(NodePlugin) {
				require(CounterPlugin) {
					require(TextCollectionPlugin) {
						install(object : RaptorPlugin {

							override fun RaptorPluginCompletionScope.complete() {
								assertEquals(
									expected = """
							[component registry] ->
								[counter] ->
									counter (1) -> 
										[any] -> counter extension
								[dummy] ->
									dummy (z)
									dummy (1) -> 
										[any] -> dummy extension
									dummy (a)
								[node] ->
									node (root) -> 
										[component registry] ->
											[node] ->
												node (a) -> 
													[any] -> node extension
													[component registry] ->
														[node] ->
															node (a1)
															node (a2)
												node (b)
												node (c)
								[root] -> root
								[text collection] -> text collection (foo)
						""".trimIndent(),
									actual = componentRegistry.toString()
								)

								assertEquals(
									expected = """
							[property registry] ->
								[count] -> 1
								[root node] ->
									node(root) ->
										node(a) ->
											node(a1)
											node(a2)
										node(b)
										node(c)
								[text] -> foo
						""".trimIndent(),
									actual = propertyRegistry.toString()
								)
							}


							override fun RaptorPluginInstallationScope.install() {
								componentRegistry.register(DummyComponent.key, DummyComponent("z"))
								componentRegistry.register(DummyComponent.key, DummyComponent("1").apply {
									extensions[anyComponentExtensionKey] = "dummy extension"
								})
								componentRegistry.register(DummyComponent.key, DummyComponent("a"))
							}
						})
					}
				}
			}
		}
	}


	@Test
	fun testRaptorToString() {
		val raptor = raptor {
			install(TextCollectionPlugin)
			textCollection.all {
				append("foo")
			}

			install(CounterPlugin)
			counter {
				increment()
			}
		}

		assertEquals(
			expected = """
				[raptor] ->
					[context] ->
						[property set] ->
							[count] -> 1
							[text] -> foo
			""".trimIndent(),
			actual = raptor.toString()
		)
	}
}
