package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.json.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


private const val queryKey = "query"
private const val operationNameKey = "operationName"
private const val variablesKey = "variables"
private const val errorsKey = "errors"
private const val messageKey = "message"

private val graphqlContentType = ContentType("application", "graphql")


/**
 * Outcome of interpreting an incoming GraphQL HTTP request.
 *
 * Either a successfully extracted GraphQL request ([Parsed]) or a client-facing rejection
 * ([Bad]) carrying the HTTP status and message to return.
 */
internal sealed interface GraphRequestParseResult {

	data class Parsed(
		val query: String,
		val operationName: String?,
		val variableValues: Map<String, Any?>,
	) : GraphRequestParseResult

	data class Bad(
		val status: HttpStatusCode,
		val message: String,
	) : GraphRequestParseResult
}


/**
 * Extracts the GraphQL query, operation name and variables from the raw HTTP request inputs
 * according to the GraphQL-over-HTTP conventions used by this route.
 *
 * Never throws for malformed input: invalid JSON, a missing/non-string query, a non-string
 * operation name, non-object variables, or a GET carrying a body-only content type are all
 * reported as [GraphRequestParseResult.Bad] with an appropriate 4xx status and a client-facing
 * message.
 */
internal fun parseGraphRequest(
	method: HttpMethod,
	contentType: ContentType,
	queryParameters: Parameters,
	bodyText: String,
): GraphRequestParseResult =
	when {
		contentType.match(graphqlContentType) -> parseGraphqlContentType(method = method, bodyText = bodyText)
		method == HttpMethod.Get -> parseGetRequest(queryParameters = queryParameters)
		else -> parseJsonBody(bodyText = bodyText)
	}


private fun parseGraphqlContentType(method: HttpMethod, bodyText: String): GraphRequestParseResult {
	if (method == HttpMethod.Get)
		return GraphRequestParseResult.Bad(
			status = HttpStatusCode.MethodNotAllowed,
			message = "A GET request carries no body. Provide the query in the '$queryKey' query-string parameter instead.",
		)

	return GraphRequestParseResult.Parsed(query = bodyText, operationName = null, variableValues = emptyMap())
}


private fun parseGetRequest(queryParameters: Parameters): GraphRequestParseResult {
	val query = queryParameters[queryKey]
		?: return GraphRequestParseResult.Bad(
			status = HttpStatusCode.BadRequest,
			message = "The '$queryKey' query-string parameter is required.",
		)
	val operationName = queryParameters[operationNameKey]

	val rawVariables = queryParameters[variablesKey]
	val variableValues: Map<String, Any?> = when (rawVariables) {
		null -> emptyMap()
		else -> try {
			JsonParser.default.parseMap(rawVariables)
		}
		catch (e: JsonException) {
			return GraphRequestParseResult.Bad(
				status = HttpStatusCode.BadRequest,
				message = "The '$variablesKey' query-string parameter is not valid JSON: ${e.message}",
			)
		}
	}

	return GraphRequestParseResult.Parsed(query = query, operationName = operationName, variableValues = variableValues)
}


private fun parseJsonBody(bodyText: String): GraphRequestParseResult {
	val json = try {
		JsonParser.default.parseMap(bodyText)
	}
	catch (e: JsonException) {
		return GraphRequestParseResult.Bad(
			status = HttpStatusCode.BadRequest,
			message = "The request body is not valid JSON: ${e.message}",
		)
	}

	val query = json[queryKey] as? String
		?: return GraphRequestParseResult.Bad(
			status = HttpStatusCode.BadRequest,
			message = "The '$queryKey' property is required and must be a string.",
		)

	val operationName = when (val rawOperationName = json[operationNameKey]) {
		null -> null
		is String -> rawOperationName
		else -> return GraphRequestParseResult.Bad(
			status = HttpStatusCode.BadRequest,
			message = "The '$operationNameKey' property must be a string.",
		)
	}

	val variableValues = when (val rawVariables = json[variablesKey]) {
		null -> emptyMap()
		is Map<*, *> -> {
			@Suppress("UNCHECKED_CAST")
			rawVariables as Map<String, Any?>
		}
		else -> return GraphRequestParseResult.Bad(
			status = HttpStatusCode.BadRequest,
			message = "The '$variablesKey' property must be a JSON object.",
		)
	}

	return GraphRequestParseResult.Parsed(query = query, operationName = operationName, variableValues = variableValues)
}


/**
 * Resolves the operation the request targets within [document].
 *
 * When [operationName] is given, returns the matching top-level operation (or `null` if none
 * matches). When [operationName] is `null`, returns the sole operation in the document, or
 * `null` when the document holds zero or more than one operation.
 */
internal fun resolveGraphOperation(document: GDocument, operationName: String?): GOperationDefinition? =
	when (operationName) {
		null -> document.definitions.filterIsInstance<GOperationDefinition>().singleOrNull()
		else -> document.operation(operationName)
	}


internal class GraphRoute(
	private val executionHook: RaptorGraphKtorRouteExecutionHook?,
	private val graph: RaptorGraph,
) {

	suspend fun handle(call: ApplicationCall) {
		val context = call.raptorContext

		val parsed = when (val parseResult = parseGraphRequest(
			method = call.request.httpMethod,
			contentType = call.request.contentType(),
			queryParameters = call.request.queryParameters,
			bodyText = call.receiveText(),
		)) {
			is GraphRequestParseResult.Bad -> {
				call.respondGraphError(status = parseResult.status, message = parseResult.message)

				return
			}
			is GraphRequestParseResult.Parsed -> parseResult
		}

		val documentResult = graph.parse(GDocumentSource.of(parsed.query, name = "query"))

		if (call.request.httpMethod == HttpMethod.Get) {
			val type = documentResult.valueOrNull()?.let { resolveGraphOperation(it, parsed.operationName)?.type }
			if (type == GOperationType.mutation || type == GOperationType.subscription) {
				call.respondGraphError(
					status = HttpStatusCode.MethodNotAllowed,
					message = "Only query operations may be executed over GET. Use POST for mutations and subscriptions.",
				)

				return
			}
		}

		val result = executeDocument(context = context, documentResult = documentResult, parsed = parsed)

		call.respondJson(status = HttpStatusCode.OK, value = result)
	}


	/**
	 * Executes [documentResult] and serializes the outcome for the response body.
	 *
	 * The returned map is always a valid GraphQL response: a parse, validation or execution
	 * failure is serialized into an `errors` array rather than surfaced as an exception, so the
	 * caller responds with HTTP 200 in every case.
	 */
	private suspend fun executeDocument(
		context: RaptorTransactionContext,
		documentResult: GResult<GDocument>,
		parsed: GraphRequestParseResult.Parsed,
	): Map<String, Any?> =
		documentResult
			.flatMapValue { document ->
				when (executionHook) {
					null -> graph.execute(
						document = document,
						operationName = parsed.operationName,
						variableValues = parsed.variableValues,
						context = context,
					)

					else -> {
						executionHook(
							context,
							RaptorKtorGraphRequest(
								document = document,
								operationName = parsed.operationName,
								query = parsed.query,
								variableValues = parsed.variableValues,
							)
						) { request ->
							graph.execute(
								document = request.document,
								operationName = request.operationName,
								variableValues = request.variableValues,
								context = context,
							)
						}
					}
				}
			}
			.let { graph.serialize(it) }


	internal companion object {

		val propertyKey = RaptorPropertyKey<GraphRoute>("graph route")
	}
}


private suspend fun ApplicationCall.respondJson(status: HttpStatusCode, value: Any?) =
	respondText(
		text = JsonSerializer.default.serializeValue(value),
		contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8),
		status = status,
	)


private suspend fun ApplicationCall.respondGraphError(status: HttpStatusCode, message: String) =
	respondJson(status = status, value = mapOf(errorsKey to listOf(mapOf(messageKey to message))))
