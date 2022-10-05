package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


// FIXME Add ServerFailure & InvalidValueException handlers.
internal class ExceptionHandler : GExceptionHandler {

	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError {
		//return GError(message = "FIXME")

//		val failure = exception as? ServerFailure ?: ServerFailure.internal(exception)
		// FIXME
		exception.printStackTrace()
		//LoggerFactory.getLogger(ExceptionHandler::class.java).error("Endpoint failure", failure) // FIXME
//
//		val extensions = hashMapOf(
//			"code" to failure.code,
//		)
//
//		if (failure.userMessage != failure.developerMessage)
//			extensions["userMessage"] = failure.userMessage

		// FIXME origin/locations/nodes/user message
		return GError(
			message = exception.message.orEmpty(),
			extensions = emptyMap(),
		)
	}
}
