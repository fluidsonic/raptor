package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*
import kotlin.reflect.*


public data class RaptorAggregateProjectionEvent<
	out ProjectionId : RaptorAggregateProjectionId,
	out Projection : RaptorProjection<ProjectionId>,
	out Change : RaptorAggregateChange<ProjectionId>,
	>(
	val change: Change,
	override val id: RaptorAggregateEventId,
	val isReplay: Boolean, // FIXME rm
	val previousProjection: Projection? = null,
	val projection: Projection?,
	val timestamp: Timestamp,
	val version: Int,
	val lastVersionInBatch: Int = version,
) : RaptorEntity<RaptorAggregateEventId> {

	init {
		require(previousProjection != null || projection != null) { "At least one of 'projection' or 'previousProjection' must be set." }
	}


	val projectionId: ProjectionId
		get() = checkNotNull(projection?.id ?: previousProjection?.id)
}


public fun <Id : RaptorAggregateProjectionId, Projection : RaptorProjection<Id>, Change : RaptorAggregateChange<Id>>
	RaptorAggregateProjectionEvent<Id, Projection, *>.withChangeOrNull(
	changeClass: KClass<Change>,
): RaptorAggregateProjectionEvent<Id, Projection, Change>? {
	if (!changeClass.isInstance(change))
		return null

	@Suppress("UNCHECKED_CAST")
	return this as RaptorAggregateProjectionEvent<Id, Projection, Change>
}


public fun <Id : RaptorAggregateProjectionId, Projection : RaptorProjection<Id>, Change : RaptorAggregateChange<Id>>
	RaptorAggregateProjectionEvent<*, *, *>.castOrNull(
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
	changeClass: KClass<Change>,
): RaptorAggregateProjectionEvent<Id, Projection, Change>? {
	val previousProjection: RaptorProjection<*>? = previousProjection
	val projection: RaptorProjection<*>? = projection

	if (changeClass.isInstance(change)
		&& (previousProjection == null || idClass.isInstance(previousProjection.id))
		&& (projection == null || idClass.isInstance(projection.id))
		&& (previousProjection == null || projectionClass.isInstance(previousProjection))
		&& (projection == null || projectionClass.isInstance(projection))
	)
		@Suppress("UNCHECKED_CAST")
		return this as RaptorAggregateProjectionEvent<Id, Projection, Change>

	return null
}
