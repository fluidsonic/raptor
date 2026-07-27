package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * Round-trip coercion of the five built-in scalars through all three entry points:
 * output serialization, literal input coercion and variable input coercion.
 */
class BuiltinScalarCoercionTests {

	private val fixture = graphFixture {
		definitions.add(
			graphOperationDefinition<Boolean>(name = "constBoolean", operationType = RaptorGraphOperationType.query) {
				resolver { true }
			},
			graphOperationDefinition<Boolean>(name = "echoBoolean", operationType = RaptorGraphOperationType.query) {
				val value by argument<Boolean>()

				resolver { value }
			},
			graphOperationDefinition<Double>(name = "constFloat", operationType = RaptorGraphOperationType.query) {
				resolver { 1.5 }
			},
			graphOperationDefinition<Double>(name = "echoFloat", operationType = RaptorGraphOperationType.query) {
				val value by argument<Double>()

				resolver { value }
			},
			graphOperationDefinition<Double>(name = "nanFloat", operationType = RaptorGraphOperationType.query) {
				resolver { Double.NaN }
			},
			graphOperationDefinition<Double>(name = "infiniteFloat", operationType = RaptorGraphOperationType.query) {
				resolver { Double.POSITIVE_INFINITY }
			},
			graphOperationDefinition<Int>(name = "constInt", operationType = RaptorGraphOperationType.query) {
				resolver { 42 }
			},
			graphOperationDefinition<Int>(name = "echoInt", operationType = RaptorGraphOperationType.query) {
				val value by argument<Int>()

				resolver { value }
			},
			graphOperationDefinition<String>(name = "constString", operationType = RaptorGraphOperationType.query) {
				resolver { "hello" }
			},
			graphOperationDefinition<String>(name = "echoString", operationType = RaptorGraphOperationType.query) {
				val value by argument<String>()

				resolver { value }
			},
		)
	}


	@Test
	fun booleanOutput() {
		assertEquals(actual = fixture.executeData("{constBoolean}"), expected = mapOf("constBoolean" to true))
	}


	@Test
	fun booleanLiteralInput() {
		assertEquals(actual = fixture.executeData("{echoBoolean(value: false)}"), expected = mapOf("echoBoolean" to false))
	}


	@Test
	fun booleanVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Boolean!) {echoBoolean(value: \$value)}",
				variableValues = mapOf("value" to false),
			),
			expected = mapOf("echoBoolean" to false),
		)
	}


	@Test
	fun floatOutput() {
		assertEquals(actual = fixture.executeData("{constFloat}"), expected = mapOf("constFloat" to 1.5))
	}


	@Test
	fun floatLiteralInput() {
		assertEquals(actual = fixture.executeData("{echoFloat(value: 2.25)}"), expected = mapOf("echoFloat" to 2.25))
	}


	@Test
	fun floatVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Float!) {echoFloat(value: \$value)}",
				variableValues = mapOf("value" to 2.25),
			),
			expected = mapOf("echoFloat" to 2.25),
		)
	}


	// `Float` cannot represent `NaN` or the infinities, so output coercion must reject them instead of
	// emitting a value no JSON document can carry. Both fields are non-null, so the rejection nulls the
	// whole `data` entry rather than just the field.
	@Test
	fun nonFiniteFloatOutputProducesClientError() {
		val nan = fixture.executeExpectingClientErrors(query = "{nanFloat}", label = "FLOAT NAN OUTPUT")
		assertNull(actual = nan["data"], message = "expected 'data' to be null in $nan")

		val infinity = fixture.executeExpectingClientErrors(query = "{infiniteFloat}", label = "FLOAT INFINITY OUTPUT")
		assertNull(actual = infinity["data"], message = "expected 'data' to be null in $infinity")
	}


	@Test
	fun intOutput() {
		assertEquals(actual = fixture.executeData("{constInt}"), expected = mapOf("constInt" to 42))
	}


	@Test
	fun intLiteralInput() {
		assertEquals(actual = fixture.executeData("{echoInt(value: 123)}"), expected = mapOf("echoInt" to 123))
	}


	@Test
	fun intVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Int!) {echoInt(value: \$value)}",
				variableValues = mapOf("value" to 123),
			),
			expected = mapOf("echoInt" to 123),
		)
	}


	@Test
	fun stringOutput() {
		assertEquals(actual = fixture.executeData("{constString}"), expected = mapOf("constString" to "hello"))
	}


	@Test
	fun stringLiteralInput() {
		assertEquals(actual = fixture.executeData("""{echoString(value: "text")}"""), expected = mapOf("echoString" to "text"))
	}


	@Test
	fun stringVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: String!) {echoString(value: \$value)}",
				variableValues = mapOf("value" to "text"),
			),
			expected = mapOf("echoString" to "text"),
		)
	}
}
