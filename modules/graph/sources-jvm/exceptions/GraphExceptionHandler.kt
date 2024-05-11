package io.fluidsonic.raptor.graph

import kotlin.reflect.*


internal class GraphExceptionHandler<Exception : Throwable>(
	val exceptionClass: KClass<Exception>,
	val handle: RaptorGraphExceptionHandlerContext.(exception: Exception) -> RaptorGraphError,
) {

	override fun toString(): String =
		"handler for exception $exceptionClass"
}
