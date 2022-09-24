package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.json.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlin.system.*
import org.slf4j.*


internal class GraphRoute(
	internal val system: GraphSystem,
) {

	suspend fun handle(call: ApplicationCall) {
		// FIXME rewrite JSON parsing
		// FIXME disallow mutation for GET

		val context = DefaultRaptorGraphContext(
			parent = call.raptorContext,
			system = system
		)

		val query: String
		val operationName: String?
		val variableValues: Map<String, Any?>

		if (call.request.contentType().match(ContentType("application", "graphql"))) {
			query = call.receiveText()
			operationName = null
			variableValues = emptyMap()
		}
		else { // FIXME check contentType is json or not specified
			val json = JsonParser.default.parseMap(call.receiveText())

			query = json["query"] as String
			operationName = json["operationName"] as String?
			variableValues = (json["variables"] as Map<String, Any?>?).orEmpty()
		}

		val result: Map<String, Any?>
		val time = measureTimeMillis {
			result = system.execute(
				documentSource = GDocumentSource.of(query, name = "query"),
				operationName = operationName,
				variableValues = variableValues,
				context = context
			)
		}
		// FIXME
		LoggerFactory.getLogger(GraphRoute::class.java).info("Execution took ${time}ms")

		call.respondText(
			text = JsonSerializer.default.serializeValue(result),
			contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
		)
	}


	internal companion object {

		val propertyKey = RaptorPropertyKey<GraphRoute>("graph route")
	}
}
