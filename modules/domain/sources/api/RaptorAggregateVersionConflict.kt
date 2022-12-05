package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*


@OptIn(ExperimentalCoroutinesApi::class)
public class RaptorAggregateVersionConflict(
	message: String,
	cause: Throwable? = null,
) : Exception(message, cause), CopyableThrowable<RaptorAggregateVersionConflict> {

	override fun createCopy(): RaptorAggregateVersionConflict =
		RaptorAggregateVersionConflict(message = checkNotNull(message), this)
}
