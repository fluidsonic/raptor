package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * A scalar parser that rejects its input via `invalid(…)` must surface a client-facing GraphQL
 * error instead of letting an exception escape the executor.
 */
class InvalidInputTests {

	private val fixture = graphFixture {
		definitions.newScalar<Restricted>(name = "Restricted") {
			parseString { text ->
				if (text != "ok") invalid("only 'ok' is allowed")

				Restricted(text)
			}
			serialize { it.text }
		}

		definitions.add(
			graphOperationDefinition<Restricted>(name = "echoRestricted", operationType = RaptorGraphOperationType.query) {
				val value by argument<Restricted>()

				resolver { value }
			},
		)
	}


	@Test
	fun validInputIsAccepted() {
		assertEquals(actual = fixture.executeData("""{echoRestricted(value: "ok")}"""), expected = mapOf("echoRestricted" to "ok"))
	}


	@Test
	fun invalidLiteralInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = """{echoRestricted(value: "nope")}""",
			label = "SCALAR INVALID LITERAL",
		)
	}


	@Test
	fun invalidVariableInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = "query(\$value: Restricted!) {echoRestricted(value: \$value)}",
			variableValues = mapOf("value" to "nope"),
			label = "SCALAR INVALID VARIABLE",
		)
	}


	private data class Restricted(
		val text: String,
	)
}
