package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


// FIXME
public data class RaptorAggregateProjectionEvent<
	ProjectionId : RaptorAggregateProjectionId,
	Projection : RaptorProjection<ProjectionId>,
	Change : RaptorAggregateChange<ProjectionId>,
	>(
	val change: Change,
	override val id: RaptorAggregateEventId,
	val previousProjection: Projection? = null,
	val projection: Projection,
	val timestamp: Timestamp,
	val version: Int,
) : RaptorEntity<RaptorAggregateEventId>
