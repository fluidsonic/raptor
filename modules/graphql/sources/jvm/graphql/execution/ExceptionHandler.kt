package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.GraphSystem
import org.slf4j.*


internal class ExceptionHandler : GExceptionHandler {

	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError {
		val failure = exception as? ServerFailure ?: ServerFailure.internal(exception)
		LoggerFactory.getLogger(GraphSystem::class.java).error("Endpoint failure", failure) // FIXME

		// FIXME origin/locations/nodes
		return GError(
			message = failure.developerMessage,
			extensions = mapOf(
				"code" to failure.code,
				"userMessage" to failure.userMessage
			)
		)
	}
}
