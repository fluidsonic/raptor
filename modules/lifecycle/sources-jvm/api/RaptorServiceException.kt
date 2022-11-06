package io.fluidsonic.raptor.lifecycle


internal class RaptorServiceException(
	val service: RaptorService,
	message: String,
	cause: Throwable? = null,
) : RuntimeException(message, cause)
