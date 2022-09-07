package io.fluidsonic.raptor.ktor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import org.slf4j.*


internal class ExceptionHandler : GExceptionHandler {

	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError {
		val failure = exception as? ServerFailure ?: ServerFailure.internal(exception)
		LoggerFactory.getLogger(GraphSystem::class.java).error("Endpoint failure", failure) // FIXME

		val extensions = hashMapOf(
			"code" to failure.code,
		)

		if (failure.userMessage != failure.developerMessage)
			extensions["userMessage"] = failure.userMessage

		// FIXME origin/locations/nodes
		return GError(
			message = failure.developerMessage,
			extensions = extensions,
		)
	}
}
