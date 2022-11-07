package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


internal class GraphExceptionHandler<Exception : Throwable>(
	val exceptionClass: KClass<Exception>,
	val handle: RaptorTransactionContext.(exception: Exception) -> RaptorGraphError,
) {

	override fun toString(): String =
		"handler for exception $exceptionClass"
}
