package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * Argument delegates can be post-processed with `map` and `validate`
 * (`RaptorGraphArgumentDelegate.kt`). Both run inside a `RaptorGraphInputScope`, so a `validate`
 * that calls `invalid(…)` must reach the client as an error rather than as an escaping exception.
 *
 * The `Maybe`-flavoured variants (`mapValue`, `validateValue`, …) are deliberately not covered —
 * `Maybe` support is being removed.
 */
class ArgumentTransformTests {

	private val fixture = graphFixture {
		definitions.add(
			graphOperationDefinition<Int>(name = "wordLength", operationType = RaptorGraphOperationType.query) {
				val value by argument<String>().map { it.length }

				resolver { value }
			},
			graphOperationDefinition<Int>(name = "doublePositive", operationType = RaptorGraphOperationType.query) {
				val value by argument<Int>()
					.validate { if (it <= 0) invalid("must be positive") }
					.map { it * 2 }

				resolver { value }
			},
		)
	}


	@Test
	fun mapTransformAppliesToLiteralInput() {
		assertEquals(actual = fixture.executeData("""{wordLength(value: "abcde")}"""), expected = mapOf("wordLength" to 5))
	}


	@Test
	fun mapTransformAppliesToVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: String!) {wordLength(value: \$value)}",
				variableValues = mapOf("value" to "abcde"),
			),
			expected = mapOf("wordLength" to 5),
		)
	}


	@Test
	fun validateTransformAcceptsValidLiteralInput() {
		assertEquals(actual = fixture.executeData("{doublePositive(value: 21)}"), expected = mapOf("doublePositive" to 42))
	}


	@Test
	fun validateTransformAcceptsValidVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Int!) {doublePositive(value: \$value)}",
				variableValues = mapOf("value" to 21),
			),
			expected = mapOf("doublePositive" to 42),
		)
	}


	@Test
	fun validateRejectionOfLiteralInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = "{doublePositive(value: 0)}",
			label = "ARGUMENT VALIDATE LITERAL",
			expectedErrorCode = "invalid value",
		)
	}


	@Test
	fun validateRejectionOfVariableInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = "query(\$value: Int!) {doublePositive(value: \$value)}",
			variableValues = mapOf("value" to 0),
			label = "ARGUMENT VALIDATE VARIABLE",
			expectedErrorCode = "invalid value",
		)
	}
}
