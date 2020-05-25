package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.json.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*


internal class GraphRoute(
	private val system: GraphSystem
) {

	suspend fun handle(call: ApplicationCall) {
		// application/graphql -> body == query
		// FIXME require JSON header
		// FIXME error handling
		// FIXME rewrite
		// FIXME disallow mutation for GET

		val scope = RaptorGraphScopeImpl(
			context = call.raptorContext as RaptorTransactionContext // FIXME nope.
		)
		val schema = system.schema

		val query: String
		val operationName: String?
		val variables: Map<String, Any?>

		if (call.request.contentType().match(ContentType("application", "graphql"))) {
			query = call.receiveText()
			operationName = null
			variables = emptyMap()
		}
		else {
			val json = JsonParser.default.parseMap(call.receiveText())

			query = json["query"] as String
			operationName = json["operationName"] as String?
			variables = (json["variables"] as Map<String, Any?>?).orEmpty()
		}

		val rootResolver = GRootResolver<RaptorGraphScope> {
			when (operationType) {
				GOperationType.mutation -> GraphOperation.Mutation
				GOperationType.query -> GraphOperation.Query
				GOperationType.subscription -> error("Subscriptions are not supported.")
			}
		}

		val result = try {
			GDocument
				.parse(query, "query")
				.execute(
					schema = schema,
					rootResolver = rootResolver,
					environment = scope,
					operationName = operationName,
					variableValues = variables,
					defaultResolver = system.createFieldResolver(scope = scope), // FIXME why do I have to pass the scope?
					nodeInputCoercion = system.createNodeInputCoercion(scope = scope) // FIXME why do I have to pass the scope?
				)
		}
		catch (e: GError) { // FIXME other errors
			mapOf("errors" to listOf(
				mapOf("message" to e.message)
			), "data" to null) // FIXME do in GQL lib
		}

		call.respondText(JsonSerializer.default.serializeValue(result), contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8))
	}


	object PropertyKey : RaptorPropertyKey<GraphRoute> {

		override fun toString() = "graph route"
	}
}
