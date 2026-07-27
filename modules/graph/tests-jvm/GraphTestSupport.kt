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

	/**
	 * Parses and executes [query], returning the serialized result — including any `errors` entry.
	 *
	 * Mirrors what a client observes through the production Ktor route: a parse or validation failure
	 * is serialized into the `errors` entry rather than thrown, so the returned map is always a valid
	 * GraphQL response.
	 */
	fun execute(query: String, variableValues: Map<String, Any?> = emptyMap()): Map<String, Any?> {
		val documentResult = graph.parse(GDocumentSource.of(query, name = "query"))
		val context = raptor.transaction().context
		val result = runBlocking {
			documentResult.flatMapValue { document ->
				graph.execute(document = document, variableValues = variableValues, context = context)
			}
		}

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
 * The full payload is printed under [label]. The message wording is deliberately never asserted —
 * it is expected to change — so the printed baseline is the only record of it. [expectedErrorCode],
 * when given, additionally asserts that every error carries exactly that `extensions.code`; that
 * part *is* a client contract. Leave it `null` for rejections produced by fluid-graphql itself
 * (output coercion, document validation), which carry no code.
 */
internal fun GraphFixture.executeExpectingClientErrors(
	query: String,
	variableValues: Map<String, Any?> = emptyMap(),
	label: String,
	expectedErrorCode: String? = null,
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

	if (expectedErrorCode !== null)
		for (error in errors) {
			assertTrue(error is Map<*, *>, "expected each error to be a map but was $error")

			val extensions = assertNotNull(error["extensions"], "expected 'extensions' in error $error of $serialized")
			assertTrue(extensions is Map<*, *>, "expected 'extensions' to be a map but was $extensions")
			assertEquals(actual = extensions["code"], expected = expectedErrorCode, message = "in error $error of $serialized")
		}

	return serialized
}
