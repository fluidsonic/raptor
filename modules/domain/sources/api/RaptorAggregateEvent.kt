package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.event.*
import io.fluidsonic.time.*


public data class RaptorAggregateEvent<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val change: Change,
	val id: RaptorAggregateEventId,
	val timestamp: Timestamp,
	val version: Int,
	val lastVersionInBatch: Int = version,
) : RaptorEvent {

	init {
		require(version > 0) { "'version' must be positive: $version" }
		require(lastVersionInBatch >= version) { "'lastVersionInBatch' must be greater than or equal to 'version': $lastVersionInBatch >= $version" }
	}
}
