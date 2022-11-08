package io.fluidsonic.raptor.domain


public class RaptorAggregateVersionConflict(
	message: String,
	cause: Throwable? = null,
) : Exception(cause)
