package io.fluidsonic.raptor.cqrs

import io.fluidsonic.time.*


// FIXME rn to RaptorAggregateEvent?
public data class RaptorEvent<
	out AggregateId : RaptorAggregateId,
	out AggregateEvent : RaptorAggregateEvent<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val data: AggregateEvent,
	override val id: RaptorEventId,
	val timestamp: Timestamp,
	val version: Int,
) : RaptorEntity<RaptorEventId> {

	init {
		require(version > 0) { "Version number must be positive: $version" }
	}
}
