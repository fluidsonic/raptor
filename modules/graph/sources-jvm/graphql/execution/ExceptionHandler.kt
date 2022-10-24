package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


// FIXME Rework exception handling.
internal class ExceptionHandler : GExceptionHandler {

	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError {
		//return GError(message = "TODO")

//		val failure = exception as? ServerFailure ?: ServerFailure.internal(exception)
		// TODO
		exception.printStackTrace()
		//LoggerFactory.getLogger(ExceptionHandler::class.java).error("Endpoint failure", failure) // TODO
//
//		val extensions = hashMapOf(
//			"code" to failure.code,
//		)
//
//		if (failure.userMessage != failure.developerMessage)
//			extensions["userMessage"] = failure.userMessage

		// TODO add origin/locations/nodes/user message
		return GError(
			message = exception.message.orEmpty(),
			extensions = emptyMap(),
		)
	}
}
