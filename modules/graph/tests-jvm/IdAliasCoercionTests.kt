package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * Round-trip coercion of an `ID` alias over a value class wrapping [String].
 *
 * GraphQL's `ID` accepts both `Int` and `String` literals, so both are covered.
 */
class IdAliasCoercionTests {

	private val fixture = graphFixture {
		definitions.newIdAlias<UserId> {
			parse { UserId(it) }
			serialize { it.value }
		}

		definitions.add(
			graphOperationDefinition<UserId>(name = "constUserId", operationType = RaptorGraphOperationType.query) {
				resolver { UserId("u-1") }
			},
			graphOperationDefinition<UserId>(name = "echoUserId", operationType = RaptorGraphOperationType.query) {
				val value by argument<UserId>()

				resolver { value }
			},
		)
	}


	@Test
	fun idAliasOutput() {
		assertEquals(actual = fixture.executeData("{constUserId}"), expected = mapOf("constUserId" to "u-1"))
	}


	@Test
	fun idAliasStringLiteralInput() {
		assertEquals(
			actual = fixture.executeData("""{echoUserId(value: "u-7")}"""),
			expected = mapOf("echoUserId" to "u-7"),
		)
	}


	@Test
	fun idAliasIntLiteralInput() {
		assertEquals(
			actual = fixture.executeData("{echoUserId(value: 7)}"),
			expected = mapOf("echoUserId" to "7"),
		)
	}


	@Test
	fun idAliasVariableInput() {
		assertEquals(
			actual = fixture.executeData(
				query = "query(\$value: ID!) {echoUserId(value: \$value)}",
				variableValues = mapOf("value" to "u-7"),
			),
			expected = mapOf("echoUserId" to "u-7"),
		)
	}
}


@JvmInline
private value class UserId(val value: String)
