package io.fluidsonic.raptor.domain


public data class RaptorAggregateProjectionEventBatch<
	out ProjectionId : RaptorAggregateProjectionId,
	out Projection : RaptorProjection<ProjectionId>,
	out Change : RaptorAggregateChange<ProjectionId>,
	>(
	val events: List<RaptorAggregateProjectionEvent<ProjectionId, Projection, Change>>,
	val isReplay: Boolean, // TODO calculate?
	val projectionId: ProjectionId, // TODO calculate?
	val version: Int, // TODO calculate?
) {

	init {
		require(events.isNotEmpty()) { "'events' must not be empty." }
		require(events.all { it.projectionId == projectionId }) { "'events' must all have the same 'projectionId' as the batch: $this" }
		require(events.all { it.isReplay == isReplay }) { "'events' must all have the same 'isReplay' as the batch: $this" }
		require(events.all { it.lastVersionInBatch == version }) { "'events' must all have the same 'lastVersionInBatch' as the batch 'version': $this" }
	}


	val projection: Projection?
		get() = events.last().projection


	val previousProjection: Projection?
		get() = events.first().previousProjection
}
