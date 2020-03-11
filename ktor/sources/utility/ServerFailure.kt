package io.fluidsonic.raptor


class ServerFailure private constructor(
	val code: String,
	val developerMessage: String,
	val internalMessage: String,
	val userMessage: String,
	cause: Throwable? = null
) : Exception(internalMessage, cause) {

	companion object {

		const val genericUserMessage = "Looks like we're having some trouble right now.\nPlease try again soon."


		fun internal(
			cause: Throwable
		) =
			ServerFailure(
				code = "internal",
				userMessage = genericUserMessage,
				developerMessage = genericUserMessage,
				internalMessage = cause.message ?: "Unknown cause",
				cause = cause
			)


		fun ofDeveloper(
			code: String,
			developerMessage: String,
			userMessage: String = genericUserMessage,
			internalMessage: String = developerMessage,
			cause: Throwable? = null
		) =
			ServerFailure(
				code = code,
				userMessage = userMessage,
				developerMessage = developerMessage,
				internalMessage = internalMessage,
				cause = cause
			)


		fun ofUser(
			code: String,
			userMessage: String,
			developerMessage: String = userMessage,
			internalMessage: String = developerMessage,
			cause: Throwable? = null
		) =
			ServerFailure(
				code = code,
				userMessage = userMessage,
				developerMessage = developerMessage,
				internalMessage = internalMessage,
				cause = cause
			)
	}
}
