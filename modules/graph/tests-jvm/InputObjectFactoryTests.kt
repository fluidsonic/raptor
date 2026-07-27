package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * `RaptorInputObjectGraphDefinitionBuilder.factory { }` is the only construction path for an input
 * object — there is no constructor binding. The factory body runs inside a `RaptorGraphInputScope`,
 * so it can normalize its arguments and reject them via `invalid(…)`.
 *
 * The normalization (a negative `start` is clamped to `0`) is what proves the factory body actually
 * ran instead of the arguments being bound straight onto the data class.
 */
class InputObjectFactoryTests {

	private val fixture = graphFixture {
		definitions.newInputObject<Range>(name = "Range") {
			val start by argument<Int>()
			val endInclusive by argument<Int>()

			factory {
				if (endInclusive < start) invalid("endInclusive must not be smaller than start")

				Range(start = start.coerceAtLeast(0), endInclusive = endInclusive)
			}
		}

		definitions.add(
			graphOperationDefinition<String>(name = "describeRange", operationType = RaptorGraphOperationType.query) {
				val value by argument<Range>()

				resolver { "${value.start}..${value.endInclusive} (${value.size})" }
			},
		)
	}


	@Test
	fun factoryConstructsFromLiteralInput() {
		assertEquals(
			actual = fixture.executeData("{describeRange(value: {start: -5, endInclusive: 3})}"),
			expected = mapOf("describeRange" to "0..3 (4)"),
		)
	}


	@Test
	fun factoryConstructsFromVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Range!) {describeRange(value: \$value)}",
				variableValues = mapOf("value" to mapOf("start" to -5, "endInclusive" to 3)),
			),
			expected = mapOf("describeRange" to "0..3 (4)"),
		)
	}


	@Test
	fun factoryRejectionOfLiteralInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = "{describeRange(value: {start: 5, endInclusive: 1})}",
			label = "INPUT OBJECT FACTORY LITERAL",
			expectedErrorCode = "invalid value",
		)
	}


	@Test
	fun factoryRejectionOfVariableInputProducesClientError() {
		fixture.executeExpectingClientErrors(
			query = "query(\$value: Range!) {describeRange(value: \$value)}",
			variableValues = mapOf("value" to mapOf("start" to 5, "endInclusive" to 1)),
			label = "INPUT OBJECT FACTORY VARIABLE",
			expectedErrorCode = "invalid value",
		)
	}


	private data class Range(
		val start: Int,
		val endInclusive: Int,
	) {

		val size: Int
			get() = endInclusive - start + 1
	}
}
