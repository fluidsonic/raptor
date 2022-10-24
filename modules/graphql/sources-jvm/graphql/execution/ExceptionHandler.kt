package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.ktor.*
import org.slf4j.*


internal class ExceptionHandler : GExceptionHandler {

	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError {
		val failure = exception as? ServerFailure ?: ServerFailure.internal(exception)
		LoggerFactory.getLogger(GraphSystem::class.java).error("Endpoint failure", failure) // TODO

		val extensions = hashMapOf(
			"code" to failure.code,
		)

		if (failure.userMessage != failure.developerMessage)
			extensions["userMessage"] = failure.userMessage

		// TODO origin/locations/nodes
		return GError(
			message = failure.developerMessage,
			extensions = extensions,
		)
	}
}
