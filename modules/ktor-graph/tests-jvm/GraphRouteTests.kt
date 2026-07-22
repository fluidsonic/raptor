package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.json.*
import io.fluidsonic.raptor.ktor.graph.*
import io.ktor.http.*
import kotlin.test.*


class GraphRouteTests {

	@Test
	fun testParsePostJsonWithQueryOnly() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = """{"query":"{a}"}""",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "{a}")
		assertNull(parsed.operationName)
		assertEquals(actual = parsed.variableValues, expected = emptyMap())
	}


	@Test
	fun testParsePostJsonWithOperationNameAndVariables() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = """{"query":"query A{a}","operationName":"A","variables":{"x":1}}""",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "query A{a}")
		assertEquals(actual = parsed.operationName, expected = "A")
		assertEquals(actual = parsed.variableValues, expected = mapOf<String, Any?>("x" to 1))
	}


	@Test
	fun testParsePostEmptyBodyIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Any,
			queryParameters = Parameters.Empty,
			bodyText = "",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostInvalidJsonIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = "{not json",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostInvalidJsonMessageIncludesJsonError() {
		val invalidJson = "{not json"
		val expectedDetail = try {
			JsonParser.default.parseMap(invalidJson)
			null
		}
		catch (e: JsonException) {
			e.message
		}
		assertNotNull(expectedDetail, "the JSON parser should provide an error detail")

		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = invalidJson,
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
		assertTrue(bad.message.contains(expectedDetail), "expected '${bad.message}' to contain '$expectedDetail'")
	}


	@Test
	fun testParsePostMissingQueryIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = "{}",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostNonStringQueryIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = """{"query":123}""",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostNonObjectVariablesIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = """{"query":"{a}","variables":5}""",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostNonStringOperationNameIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.Application.Json,
			queryParameters = Parameters.Empty,
			bodyText = """{"query":"{a}","operationName":123}""",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParsePostJsonWithCharsetParameter() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.parse("application/json; charset=utf-8"),
			queryParameters = Parameters.Empty,
			bodyText = """{"query":"{a}"}""",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "{a}")
	}


	@Test
	fun testParseGraphqlContentTypeWithCharsetParameter() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.parse("application/graphql; charset=utf-8"),
			queryParameters = Parameters.Empty,
			bodyText = "{a}",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "{a}")
	}


	@Test
	fun testParsePostGraphqlContentTypeUsesBodyAsQuery() {
		val result = parseGraphRequest(
			method = HttpMethod.Post,
			contentType = ContentType.parse("application/graphql"),
			queryParameters = Parameters.Empty,
			bodyText = "{a}",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "{a}")
		assertNull(parsed.operationName)
		assertEquals(actual = parsed.variableValues, expected = emptyMap())
	}


	@Test
	fun testParseGetGraphqlContentTypeIsMethodNotAllowed() {
		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.parse("application/graphql"),
			queryParameters = Parameters.Empty,
			bodyText = "",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.MethodNotAllowed)
	}


	@Test
	fun testParseGetWithQueryParameter() {
		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.Any,
			queryParameters = parametersOf("query", "{a}"),
			bodyText = "",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "{a}")
		assertNull(parsed.operationName)
		assertEquals(actual = parsed.variableValues, expected = emptyMap())
	}


	@Test
	fun testParseGetWithVariablesJsonParameter() {
		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.Any,
			queryParameters = Parameters.build {
				append("query", "query A{a}")
				append("operationName", "A")
				append("variables", """{"x":1}""")
			},
			bodyText = "",
		)

		val parsed = assertIs<GraphRequestParseResult.Parsed>(result)
		assertEquals(actual = parsed.query, expected = "query A{a}")
		assertEquals(actual = parsed.operationName, expected = "A")
		assertEquals(actual = parsed.variableValues, expected = mapOf<String, Any?>("x" to 1))
	}


	@Test
	fun testParseGetMissingQueryIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.Any,
			queryParameters = Parameters.Empty,
			bodyText = "",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParseGetWithInvalidVariablesJsonIsBadRequest() {
		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.Any,
			queryParameters = Parameters.build {
				append("query", "{a}")
				append("variables", "{not json")
			},
			bodyText = "",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
	}


	@Test
	fun testParseGetWithInvalidVariablesJsonMessageIncludesJsonError() {
		val invalidJson = "{not json"
		val expectedDetail = try {
			JsonParser.default.parseMap(invalidJson)
			null
		}
		catch (e: JsonException) {
			e.message
		}
		assertNotNull(expectedDetail, "the JSON parser should provide an error detail")

		val result = parseGraphRequest(
			method = HttpMethod.Get,
			contentType = ContentType.Any,
			queryParameters = Parameters.build {
				append("query", "{a}")
				append("variables", invalidJson)
			},
			bodyText = "",
		)

		val bad = assertIs<GraphRequestParseResult.Bad>(result)
		assertEquals(actual = bad.status, expected = HttpStatusCode.BadRequest)
		assertTrue(bad.message.contains(expectedDetail), "expected '${bad.message}' to contain '$expectedDetail'")
	}


	@Test
	fun testResolveNamedOperation() {
		val document = GDocument.parse("query A { a } query B { b }").valueOrNull()!!

		val operation = resolveGraphOperation(document = document, operationName = "A")
		assertNotNull(operation)
		assertEquals(actual = operation.type, expected = GOperationType.query)
	}


	@Test
	fun testResolveSingleAnonymousOperation() {
		val document = GDocument.parse("{ a }").valueOrNull()!!

		val operation = resolveGraphOperation(document = document, operationName = null)
		assertNotNull(operation)
		assertEquals(actual = operation.type, expected = GOperationType.query)
	}


	@Test
	fun testResolveSingleNamedOperationWithoutOperationName() {
		val document = GDocument.parse("query A { a }").valueOrNull()!!

		val operation = resolveGraphOperation(document = document, operationName = null)
		assertNotNull(operation)
		assertEquals(actual = operation.type, expected = GOperationType.query)
	}


	@Test
	fun testResolveNamedMutation() {
		val document = GDocument.parse("mutation M { m }").valueOrNull()!!

		val operation = resolveGraphOperation(document = document, operationName = "M")
		assertNotNull(operation)
		assertEquals(actual = operation.type, expected = GOperationType.mutation)
	}


	@Test
	fun testResolveAmbiguousOperationIsNull() {
		val document = GDocument.parse("query A { a } query B { b }").valueOrNull()!!

		val operation = resolveGraphOperation(document = document, operationName = null)
		assertNull(operation)
	}
}
