package tests

import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * An argument's nullability decides whether a query may leave it out. A nullable argument with no default
 * is optional and the resolver observes `null` when it is omitted; a non-null argument with no default is
 * always required and omitting it is a client error, never a resolver-visible `null`.
 */
class ArgumentNullabilityTests {

	private val fixture = graphFixture {
		definitions.add(
			graphOperationDefinition<String>(name = "describeOptional", operationType = RaptorGraphOperationType.query) {
				val value by argument<String?>()

				resolver { value ?: "absent" }
			},
			graphOperationDefinition<String>(name = "describeRequired", operationType = RaptorGraphOperationType.query) {
				val value by argument<String>()

				resolver { value }
			},
		)
	}


	@Test
	fun omittedNullableArgumentIsAbsent() {
		assertEquals(actual = fixture.executeData("{describeOptional}"), expected = mapOf("describeOptional" to "absent"))
	}


	@Test
	fun suppliedNullableArgumentIsObserved() {
		assertEquals(
			actual = fixture.executeData("""{describeOptional(value: "here")}"""),
			expected = mapOf("describeOptional" to "here"),
		)
	}


	@Test
	fun omittedNonNullArgumentProducesClientError() {
		fixture.executeExpectingClientErrors(query = "{describeRequired}", label = "ARGUMENT MISSING REQUIRED")
	}
}
