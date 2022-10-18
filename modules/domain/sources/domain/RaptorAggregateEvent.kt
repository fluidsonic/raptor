package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


public data class RaptorAggregateEvent<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val change: Change,
	override val id: RaptorAggregateEventId,
	val isReplay: Boolean,
	val timestamp: Timestamp,
	val version: Int,
	val lastVersionInBatch: Int = version,
) : RaptorEntity<RaptorAggregateEventId> {

	init {
		require(version > 0) { "'version' must be positive: $version" }
		require(lastVersionInBatch >= version) { "'lastVersionInBatch' must be greater than or equal to 'version': $lastVersionInBatch >= $version" }
	}
}
