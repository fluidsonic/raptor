package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*


/**
 * Two graphs in one application, each with its own custom scalar. Coercers must stay bound to the
 * graph that declared them and must not leak across graphs.
 */
class MultiGraphTests {

	private val raptor = raptor {
		install(RaptorTransactionPlugin)
		install(RaptorGraphPlugin)

		graphs.new {
			tag("A")

			definitions.newScalar<Alpha>(name = "Alpha") {
				parseString { Alpha(it) }
				serialize { "alpha:${it.text}" }
			}

			definitions.add(
				graphOperationDefinition<Alpha>(name = "echoAlpha", operationType = RaptorGraphOperationType.query) {
					val value by argument<Alpha>()

					resolver { value }
				},
			)
		}

		graphs.new {
			tag("B")

			definitions.newScalar<Beta>(name = "Beta") {
				parseString { Beta(it) }
				serialize { "beta:${it.text}" }
			}

			definitions.add(
				graphOperationDefinition<Beta>(name = "echoBeta", operationType = RaptorGraphOperationType.query) {
					val value by argument<Beta>()

					resolver { value }
				},
			)
		}
	}

	private val graphA = raptor.context.plugins.graph.taggedGraph("A")
	private val graphB = raptor.context.plugins.graph.taggedGraph("B")
	private val fixtureA = GraphFixture(raptor = raptor, graph = graphA)
	private val fixtureB = GraphFixture(raptor = raptor, graph = graphB)


	@Test
	fun graphACoercesItsOwnScalar() {
		assertEquals(
			actual = fixtureA.executeData("""{echoAlpha(value: "one")}"""),
			expected = mapOf("echoAlpha" to "alpha:one"),
		)
	}


	@Test
	fun graphBCoercesItsOwnScalar() {
		assertEquals(
			actual = fixtureB.executeData("""{echoBeta(value: "two")}"""),
			expected = mapOf("echoBeta" to "beta:two"),
		)
	}


	@Test
	fun graphACoercesItsOwnScalarViaVariable() {
		assertEquals(
			actual = fixtureA.executeData(
				query = "query(\$value: Alpha!) {echoAlpha(value: \$value)}",
				variableValues = mapOf("value" to "one"),
			),
			expected = mapOf("echoAlpha" to "alpha:one"),
		)
	}


	@Test
	fun graphBCoercesItsOwnScalarViaVariable() {
		assertEquals(
			actual = fixtureB.executeData(
				query = "query(\$value: Beta!) {echoBeta(value: \$value)}",
				variableValues = mapOf("value" to "two"),
			),
			expected = mapOf("echoBeta" to "beta:two"),
		)
	}


	@Test
	fun scalarsAreNotSharedBetweenGraphs() {
		assertNotNull(graphA.schema.resolveType("Alpha"), "graph A must declare 'Alpha'")
		assertNull(graphA.schema.resolveType("Beta"), "graph A must not know about 'Beta'")

		assertNotNull(graphB.schema.resolveType("Beta"), "graph B must declare 'Beta'")
		assertNull(graphB.schema.resolveType("Alpha"), "graph B must not know about 'Alpha'")
	}


	private data class Alpha(
		val text: String,
	)


	private data class Beta(
		val text: String,
	)
}
