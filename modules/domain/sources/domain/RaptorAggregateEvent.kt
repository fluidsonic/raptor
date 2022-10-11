package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


public data class RaptorAggregateEvent<
	out AggregateId : RaptorAggregateId,
	out AggregateChange : RaptorAggregateChange<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val change: AggregateChange,
	override val id: RaptorAggregateEventId,
	val isReplay: Boolean,
	val timestamp: Timestamp,
	val version: Int,
) : RaptorEntity<RaptorAggregateEventId> {

	init {
		require(version > 0) { "Version number must be positive: $version" }
	}
}
