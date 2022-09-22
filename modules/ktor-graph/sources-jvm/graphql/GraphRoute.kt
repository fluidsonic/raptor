package io.fluidsonic.raptor.ktor

import io.fluidsonic.graphql.*
import io.fluidsonic.json.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlin.system.*
import org.slf4j.*


internal class GraphRoute(
	private val graph: RaptorGraph,
) {

	suspend fun handle(call: ApplicationCall) {
		// FIXME rewrite JSON parsing
		// FIXME disallow mutation for GET

		val context = call.raptorContext

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
			result = graph.parse(GDocumentSource.of(query, name = "query"))
				.flatMapValue { document ->
					graph.execute(
						document = document,
						operationName = operationName,
						variableValues = variableValues,
						context = context,
					)
				}
				.let { graph.serialize(it) }
		}
		// FIXME
		LoggerFactory.getLogger(GraphRoute::class.java).info("Execution took ${time}ms")

		call.respondText(
			text = JsonSerializer.default.serializeValue(result),
			contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
		)
	}


	object PropertyKey : RaptorPropertyKey<GraphRoute> {

		override fun toString() = "graph route"
	}
}
