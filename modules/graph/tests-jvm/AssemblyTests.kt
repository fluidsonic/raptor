package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.graph.*
import kotlin.test.*


class AssemblyTests {

	@Test
	fun testDefinitions() {
		val raptor = raptor {
			install(RaptorGraphPlugin)

			graphs.new {
				tag("A")
				definitions {
					add(Dummy1.graphDefinition())
					add(listOf(Dummy2.graphDefinition()))
				}
				addHelloQuery()
			}

			graphs.new().apply {
				tag("B")
				definitions {
					add(Dummy3.graphDefinition())
					add(listOf(Dummy4.graphDefinition()))
				}
				addHelloQuery()
			}
		}

		val graphA = raptor.context.plugins.graph.taggedGraph("A")
		assertNotNull(graphA.schema.resolveType("Dummy1"))
		assertNotNull(graphA.schema.resolveType("Dummy2"))

		val graphB = raptor.context.plugins.graph.taggedGraph("B")
		assertNotNull(graphB.schema.resolveType("Dummy3"))
		assertNotNull(graphB.schema.resolveType("Dummy4"))
	}


	@Test
	fun testDI() {
		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorGraphPlugin)

			graphs.new().apply {
				tag("A")
				addHelloQuery()
			}

			di.provide<RaptorGraph> { context.plugins.graph.taggedGraph("A") }
		}

		assertEquals(actual = raptor.context.di.get<RaptorGraph>().tags, expected = setOf("A"))
	}


	@Test
	fun testIncludeDefaultDefinitions() {
		val raptor = raptor {
			install(RaptorGraphPlugin)

			graphs.new {
				tag("A")
				definitions.includeDefault()
				addHelloQuery()
			}

			graphs.new().apply {
				tag("B")
				addHelloQuery()
			}
		}

		val defaultTypeNames = listOf(
			"Country",
			"CountryCode",
			"Currency",
			"Duration",
			"LocalDate",
			"LocalDateTime",
			"LocalTime",
			"Locale",
			"Timestamp",
			"TimeZone",
			"Unit",
		)

		val graphA = raptor.context.plugins.graph.taggedGraph("A")
		for (name in defaultTypeNames)
			assertNotNull(graphA.schema.resolveType(name))

		val graphB = raptor.context.plugins.graph.taggedGraph("B")
		for (name in defaultTypeNames)
			assertNull(graphB.schema.resolveType(name))
	}


	@Test
	fun testNew() {
		var count = 0

		val raptor = raptor {
			install(RaptorGraphPlugin)

			graphs.new().addHelloQuery()
			graphs.new { addHelloQuery() }
			graphs {
				new().addHelloQuery()
				new { addHelloQuery() }
			}

			graphs.all {
				count++
			}
		}

		assertEquals(actual = count, expected = 4)
		assertEquals(actual = raptor.context.plugins.graph.graphs.size, expected = 4)
	}


	@Test
	fun testTagging() {
		var aTagged = 0
		var bTagged = 0
		var cTagged = 0
		var dTagged = 0

		val raptor = raptor {
			install(RaptorGraphPlugin)

			graphs.new().apply {
				tag("A")
				addHelloQuery()
			}
			graphs.new {
				tag("B", "C")
				addHelloQuery()
			}

			graphs.tagged("A") {
				aTagged += 1
			}
			graphs.tagged("B") {
				bTagged += 1
			}
			graphs.tagged("C") {
				cTagged += 1
			}
			graphs.tagged("D") {
				dTagged += 1
			}

			graphs.new().apply {
				tag("D")
				addHelloQuery()
			}
		}

		assertEquals(actual = aTagged, expected = 1)
		assertEquals(actual = bTagged, expected = 1)
		assertEquals(actual = cTagged, expected = 1)
		assertEquals(actual = dTagged, expected = 1)
		assertEquals(
			actual = raptor.context.plugins.graph.graphs.mapTo(hashSetOf()) { it.tags },
			expected = setOf<Set<Any>>(setOf("A"), setOf("B", "C"), setOf("D")),
		)
	}


	// Every graph needs a query root type to assemble. These tests are about graph creation, tagging and DI, so each
	// graph gets the same throwaway query operation — nothing here asserts on it.
	private fun RaptorGraphComponent.addHelloQuery() {
		definitions.add(
			graphOperationDefinition<String>(name = "hello", operationType = RaptorGraphOperationType.query) {
				resolver { "world" }
			},
		)
	}


	private object Dummy1 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Dummy1>("Dummy1") {
			parseString { Dummy1 }
			serialize { "dummy1" }
		}
	}


	private object Dummy2 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Dummy2>("Dummy2") {
			parseString { Dummy2 }
			serialize { "dummy2" }
		}
	}


	private object Dummy3 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Dummy3>("Dummy3") {
			parseString { Dummy3 }
			serialize { "dummy3" }
		}
	}


	private object Dummy4 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Dummy4>("Dummy4") {
			parseString { Dummy4 }
			serialize { "dummy4" }
		}
	}
}
