package io.fluidsonic.raptor


// FIXME better move to quickstart & make usage in this module configurable
public class ServerFailure private constructor(
	public val code: String,
	public val developerMessage: String,
	public val internalMessage: String,
	public val userMessage: String,
	cause: Throwable? = null,
) : Exception(internalMessage, cause) {

	public companion object {

		public const val genericUserMessage: String = "Looks like we're having some trouble right now.\nPlease try again soon."


		public fun internal(
			cause: Throwable,
		): ServerFailure =
			ServerFailure(
				code = "internal",
				userMessage = genericUserMessage,
				developerMessage = genericUserMessage,
				internalMessage = cause.message ?: "Unknown cause",
				cause = cause
			)


		public fun ofDeveloper(
			code: String,
			developerMessage: String,
			userMessage: String = genericUserMessage,
			internalMessage: String = developerMessage,
			cause: Throwable? = null,
		): ServerFailure =
			ServerFailure(
				code = code,
				userMessage = userMessage,
				developerMessage = developerMessage,
				internalMessage = internalMessage,
				cause = cause
			)


		public fun ofUser(
			code: String,
			userMessage: String,
			developerMessage: String = userMessage,
			internalMessage: String = developerMessage,
			cause: Throwable? = null,
		): ServerFailure =
			ServerFailure(
				code = code,
				userMessage = userMessage,
				developerMessage = developerMessage,
				internalMessage = internalMessage,
				cause = cause
			)
	}
}
