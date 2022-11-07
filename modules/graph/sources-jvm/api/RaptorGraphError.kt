package io.fluidsonic.raptor.graph


public data class RaptorGraphError(
	val message: String,
	val extensions: Map<String, Any?> = emptyMap(),
)
