package io.fluidsonic.raptor.domain


public class RaptorAggregateVersionConflict(
	cause: Throwable,
) : Exception(cause)
