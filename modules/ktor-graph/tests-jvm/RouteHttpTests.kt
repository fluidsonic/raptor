package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.ktor.graph.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.slf4j.*


@OptIn(ExperimentalCoroutinesApi::class)
class RouteHttpTests {

	@Test
	fun testGetQueryReturnsData() = withEngine { engine ->
		val response = engine.client.get("/graphql?query=%7Bhello%7D")

		assertEquals(actual = response.status, expected = HttpStatusCode.OK)
		val body = response.bodyAsText()
		assertTrue(body.contains("world"), "expected data in $body")
	}


	@Test
	fun testGetMutationIsMethodNotAllowed() = withEngine { engine ->
		val response = engine.client.get("/graphql?query=mutation%7Becho%7D")

		assertEquals(actual = response.status, expected = HttpStatusCode.MethodNotAllowed)
	}


	@Test
	fun testPostInvalidJsonIsBadRequest() = withEngine { engine ->
		val response = engine.postGraphql("{not json")

		assertEquals(actual = response.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testPostMissingQueryIsBadRequest() = withEngine { engine ->
		val response = engine.postGraphql("{}")

		assertEquals(actual = response.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testPostValidQueryReturnsData() = withEngine { engine ->
		val response = engine.postGraphql("""{"query":"{hello}"}""")

		assertEquals(actual = response.status, expected = HttpStatusCode.OK)
		val body = response.bodyAsText()
		assertTrue(body.contains("world"), "expected data in $body")
	}


	@Test
	fun testPostValidationErrorReturnsOkWithErrors() = withEngine { engine ->
		val response = engine.postGraphql("""{"query":"{unknownField}"}""")

		assertEquals(actual = response.status, expected = HttpStatusCode.OK)
		val body = response.bodyAsText()
		assertTrue(body.contains("errors"), "expected an errors array in $body")
	}


	@Test
	fun testGetWithoutQueryIsBadRequest() = withEngine { engine ->
		val response = engine.client.get("/graphql")

		assertEquals(actual = response.status, expected = HttpStatusCode.BadRequest)
	}


	private suspend fun TestApplicationEngine.postGraphql(body: String): HttpResponse =
		client.post("/graphql") {
			contentType(ContentType.Application.Json)
			setBody(body)
		}


	private fun withEngine(block: suspend (TestApplicationEngine) -> Unit) = runTest {
		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorGraphPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorTransactionPlugin)
			install(RaptorKtorTestPlugin)

			di.provide<Logger> { LoggerFactory.getLogger("test") }

			graphs.new {
				definitions.includeDefault()
				definitions.add(
					graphOperationDefinition<String>(name = "hello", operationType = RaptorGraphOperationType.query) {
						resolver { "world" }
					},
					graphOperationDefinition<String>(name = "echo", operationType = RaptorGraphOperationType.mutation) {
						resolver { "echoed" }
					},
				)
			}

			ktor.servers.new {
				unencryptedHosts(setOf("localhost"))

				routes.new("graphql") {
					graph()
				}
			}
		}

		raptor.lifecycle.startIn(this)
		try {
			val engine = assertNotNull(raptor.context.ktor.servers.single().testEngine, "test engine should be available after start")

			block(engine)
		}
		finally {
			raptor.lifecycle.stop()
		}
	}
}
