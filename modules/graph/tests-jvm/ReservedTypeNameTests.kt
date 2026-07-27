package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * A type definition carrying a name GraphQL reserves — one of the five built-in scalar names or any
 * `__` prefix — must be rejected by raptor's own definition-site diagnostic while the graph is being
 * assembled, not by fluid GraphQL's `GSchema` factory.
 *
 * `Boolean`, `Float`, `Int` and `String` are already covered incidentally: raptor's own uncoerced
 * mappings occupy those names, so a user definition trips the duplicate-name check first. `ID` and
 * `__`-prefixed names have no such cover, which is what these tests pin down.
 */
class ReservedTypeNameTests {

	@Test
	fun coercedScalarNamedIdIsRejected() {
		assertAssemblyRejectsTypeName("ID") {
			definitions.newScalar<Identifier>(name = "ID") {
				parseString { Identifier(it) }
				serialize { it.text }
			}
		}
	}


	@Test
	fun typeWithIntrospectionNameIsRejected() {
		assertAssemblyRejectsTypeName("__Foo") {
			definitions.newEnum<Color>(name = "__Foo")
		}
	}


	// The graph gets a query operation so that assembly reaches the reserved-name guard on its own merits
	// rather than relying on that guard running before the query-root check.
	private fun assertAssemblyRejectsTypeName(typeName: String, configure: RaptorGraphComponent.() -> Unit) {
		val exception = assertNotNull(
			runCatching {
				graphFixture {
					definitions.add(
						graphOperationDefinition<String>(name = "hello", operationType = RaptorGraphOperationType.query) {
							resolver { "world" }
						},
					)

					configure()
				}
			}.exceptionOrNull(),
			"expected assembly to fail for reserved type name '$typeName'",
		)

		assertFalse(
			exception is GErrorException,
			"expected raptor's own error but fluid GraphQL's schema factory threw: ${exception.message}",
		)
		assertIs<IllegalStateException>(exception, "expected raptor's own error but got $exception")
		assertContains(
			exception.message.orEmpty(),
			typeName,
			message = "expected the error message to name '$typeName'",
		)
	}


	private enum class Color {

		blue,
		green,
		red,
	}


	private data class Identifier(
		val text: String,
	)
}
