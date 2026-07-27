package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * A GraphQL schema must provide a query root type. A graph that declares no query operation therefore
 * has to be rejected by raptor's own diagnostic while the graph is being assembled — naming the API the
 * developer must reach for — rather than by fluid GraphQL's `Query root type must be provided.` on the
 * first request.
 */
class QueryRootTests {

	@Test
	fun graphWithOnlyTypeDefinitionsIsRejected() {
		assertAssemblyDemandsQueryOperation("only type definitions") {
			definitions.newScalar<Slug>(name = "Slug") {
				parseString { Slug(it) }
				serialize { it.text }
			}
		}
	}


	@Test
	fun graphWithOnlyMutationOperationIsRejected() {
		assertAssemblyDemandsQueryOperation("only a mutation operation") {
			definitions.add(
				graphOperationDefinition<String>(name = "touch", operationType = RaptorGraphOperationType.mutation) {
					resolver { "touched" }
				},
			)
		}
	}


	private fun assertAssemblyDemandsQueryOperation(description: String, configure: RaptorGraphComponent.() -> Unit) {
		val exception = assertNotNull(
			runCatching { graphFixture(configure) }.exceptionOrNull(),
			"expected assembly to fail for a graph with $description",
		)

		assertFalse(
			exception is GErrorException,
			"expected raptor's own error but fluid GraphQL's schema factory threw: ${exception.message}",
		)
		assertIs<IllegalStateException>(exception, "expected raptor's own error but got $exception")
		assertContains(
			exception.message.orEmpty(),
			"query root type",
			message = "expected the error message to explain that a query root type is required",
		)
		assertContains(
			exception.message.orEmpty(),
			"RaptorGraphOperationType.query",
			message = "expected the error message to point at the concrete API",
		)
	}


	private data class Slug(
		val text: String,
	)
}
