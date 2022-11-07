package io.fluidsonic.raptor.domain


public class RaptorAggregateVersionConflict(
	cause: Throwable? = null,
) : Exception(cause)
