package io.fluidsonic.raptor

import io.fluidsonic.time.*


public interface RaptorJobStatus {

	public val lastExecutionTimestamp: Timestamp?
}


public fun RaptorJobStatus(
	lastExecutionTimestamp: Timestamp?,
): RaptorJobStatus =
	RaptorJobStatusImpl(lastExecutionTimestamp = lastExecutionTimestamp)
