package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


public data class RaptorAggregateProjectionEvent<
	ProjectionId : RaptorAggregateProjectionId,
	Projection : RaptorProjection<ProjectionId>,
	Change : RaptorAggregateChange<ProjectionId>,
	>(
	val change: Change,
	override val id: RaptorAggregateEventId,
	val isReplay: Boolean,
	val previousProjection: Projection? = null,
	val projection: Projection?,
	val timestamp: Timestamp,
	val version: Int,
) : RaptorEntity<RaptorAggregateEventId> {

	init {
		require(previousProjection != null || projection != null) { "At least one of 'projection' or 'previousProjection' must be set." }
	}


	val projectionId: ProjectionId
		get() = checkNotNull(projection?.id ?: previousProjection?.id)
}
