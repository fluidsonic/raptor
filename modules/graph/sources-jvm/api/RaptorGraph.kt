package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


public interface RaptorGraph {

	public val schema: GSchema
	public val tags: Set<Any>

	public suspend fun execute(
		document: GDocument,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		context: RaptorTransactionContext,
	): GResult<Map<String, Any?>>

	public fun parse(
		source: GDocumentSource.Parsable,
	): GResult<GDocument>

	public fun serialize(
		result: GResult<Map<String, Any?>>,
	): Map<String, Any?>
}
