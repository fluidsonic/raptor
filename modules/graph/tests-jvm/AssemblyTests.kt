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
			}

			graphs.new().apply {
				tag("B")
				definitions {
					add(Dummy3.graphDefinition())
					add(listOf(Dummy4.graphDefinition()))
				}
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

			graphs.new().tag("A")

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
			}

			graphs.new().apply {
				tag("B")
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

			graphs.new()
			graphs.new {}
			graphs {
				new()
				new {}
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

			graphs.new().tag("A")
			graphs.new {
				tag("B", "C")
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

			graphs.new().tag("D")
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


	private object Dummy1 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition("Dummy1") {
			parseString { Dummy1 }
			serialize { "dummy1" }
		}
	}


	private object Dummy2 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition("Dummy2") {
			parseString { Dummy2 }
			serialize { "dummy2" }
		}
	}


	private object Dummy3 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition("Dummy3") {
			parseString { Dummy3 }
			serialize { "dummy3" }
		}
	}


	private object Dummy4 {

		fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition("Dummy4") {
			parseString { Dummy4 }
			serialize { "dummy4" }
		}
	}
}
