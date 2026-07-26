package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * Round-trip coercion of user-defined types — a custom scalar, an enum and an input object —
 * through output serialization, literal input coercion and variable input coercion.
 */
class CustomTypeCoercionTests {

	private val fixture = graphFixture {
		definitions.newScalar<Slug>(name = "Slug") {
			parseString { Slug(it) }
			serialize { it.text }
		}

		definitions.newEnum<Color>(name = "Color")

		definitions.newInputObject<Point>(name = "Point") {
			val x by argument<Int>()
			val y by argument<Int>()

			factory { Point(x = x, y = y) }
		}

		definitions.add(
			graphOperationDefinition<Slug>(name = "constSlug", operationType = RaptorGraphOperationType.query) {
				resolver { Slug("hello-world") }
			},
			graphOperationDefinition<Slug>(name = "echoSlug", operationType = RaptorGraphOperationType.query) {
				val value by argument<Slug>()

				resolver { value }
			},
			graphOperationDefinition<Color>(name = "constColor", operationType = RaptorGraphOperationType.query) {
				resolver { Color.blue }
			},
			graphOperationDefinition<Color>(name = "echoColor", operationType = RaptorGraphOperationType.query) {
				val value by argument<Color>()

				resolver { value }
			},
			graphOperationDefinition<String>(name = "describePoint", operationType = RaptorGraphOperationType.query) {
				val value by argument<Point>()

				resolver { "${value.x}/${value.y}" }
			},
		)
	}


	@Test
	fun customScalarOutput() {
		assertEquals(actual = fixture.executeData("{constSlug}"), expected = mapOf("constSlug" to "hello-world"))
	}


	@Test
	fun customScalarLiteralInput() {
		assertEquals(
			actual = fixture.executeData("""{echoSlug(value: "some-slug")}"""),
			expected = mapOf("echoSlug" to "some-slug"),
		)
	}


	@Test
	fun customScalarVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Slug!) {echoSlug(value: \$value)}",
				variableValues = mapOf("value" to "some-slug"),
			),
			expected = mapOf("echoSlug" to "some-slug"),
		)
	}


	@Test
	fun enumOutput() {
		assertEquals(actual = fixture.executeData("{constColor}"), expected = mapOf("constColor" to "blue"))
	}


	@Test
	fun enumLiteralInput() {
		assertEquals(actual = fixture.executeData("{echoColor(value: green)}"), expected = mapOf("echoColor" to "green"))
	}


	@Test
	fun enumVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Color!) {echoColor(value: \$value)}",
				variableValues = mapOf("value" to "green"),
			),
			expected = mapOf("echoColor" to "green"),
		)
	}


	@Test
	fun inputObjectLiteralInput() {
		assertEquals(
			actual = fixture.executeData("{describePoint(value: {x: 1, y: 2})}"),
			expected = mapOf("describePoint" to "1/2"),
		)
	}


	@Test
	fun inputObjectVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: Point!) {describePoint(value: \$value)}",
				variableValues = mapOf("value" to mapOf("x" to 1, "y" to 2)),
			),
			expected = mapOf("describePoint" to "1/2"),
		)
	}


	private enum class Color {

		blue,
		green,
		red,
	}


	private data class Point(
		val x: Int,
		val y: Int,
	)


	private data class Slug(
		val text: String,
	)
}
