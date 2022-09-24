package io.fluidsonic.raptor.cqrs

import io.fluidsonic.time.*


// FIXME
public data class RaptorAggregateProjectionEvent<Id, Event : RaptorAggregateEvent<Id>, Projection : RaptorProjection<Id>>(
	val data: Event,
	override val id: RaptorEventId,
	val previousProjection: Projection? = null,
	val projection: Projection,
	val timestamp: Timestamp,
	val version: Int,
) : RaptorEntity<RaptorEventId> where Id : RaptorAggregateId, Id : RaptorProjectionId
