package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*
import kotlinx.coroutines.*


/**
 * Minimal no-HTTP execution harness for `modules/graph`.
 *
 * A [GraphFixture] owns one assembled raptor application and one of its graphs. Every call to
 * [execute] runs in a fresh transaction.
 */
internal class GraphFixture(
	private val raptor: Raptor,
	val graph: RaptorGraph,
) {

	/** Parses and executes [query], returning the serialized result — including any `errors` entry. */
	fun execute(query: String, variableValues: Map<String, Any?> = emptyMap()): Map<String, Any?> {
		val document = checkNotNull(graph.parse(GDocumentSource.of(query, name = "query")).valueOrNull()) {
			"cannot parse or validate query: $query"
		}
		val context = raptor.transaction().context
		val result = runBlocking { graph.execute(document = document, variableValues = variableValues, context = context) }

		return graph.serialize(result)
	}


	/** Like [execute] but asserts the result carries no errors and returns just the `data` entry. */
	fun executeData(query: String, variableValues: Map<String, Any?> = emptyMap()): Map<String, Any?> {
		val serialized = execute(query = query, variableValues = variableValues)
		assertNull(actual = serialized["errors"], message = "expected no errors in $serialized")

		@Suppress("UNCHECKED_CAST")
		return assertNotNull(serialized["data"], "expected data in $serialized") as Map<String, Any?>
	}
}


/** Assembles a raptor application with a single graph configured by [configure]. */
internal fun graphFixture(configure: RaptorGraphComponent.() -> Unit): GraphFixture {
	val raptor = raptor {
		install(RaptorTransactionPlugin)
		install(RaptorGraphPlugin)

		graphs.new(configure)
	}

	return GraphFixture(raptor = raptor, graph = raptor.context.plugins.graph.singleGraph())
}


/**
 * Executes a query whose input is expected to be rejected, and asserts the rejection reaches the
 * client as an `errors` entry rather than as an exception escaping the executor.
 *
 * The full payload is printed under [label]. The exact wording is deliberately never asserted — it
 * is expected to change — so the printed baseline is the only record of it.
 */
internal fun GraphFixture.executeExpectingClientErrors(
	query: String,
	variableValues: Map<String, Any?> = emptyMap(),
	label: String,
): Map<String, Any?> {
	val outcome = runCatching { execute(query = query, variableValues = variableValues) }
	assertTrue(outcome.isSuccess, "execute/serialize must not throw but did: ${outcome.exceptionOrNull()}")

	val serialized = outcome.getOrThrow()

	println("=== $label PAYLOAD BEGIN ===")
	println(serialized)
	println("=== $label PAYLOAD END ===")

	val errors = assertNotNull(serialized["errors"], "expected an 'errors' entry in $serialized")
	assertTrue(errors is Collection<*>, "expected 'errors' to be a collection but was $errors")
	assertTrue(errors.isNotEmpty(), "expected at least one error in $serialized")

	return serialized
}
