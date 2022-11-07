package io.fluidsonic.raptor.domain

import kotlin.reflect.*


public interface RaptorAggregateProjectionLoaderManager {

	// FIXME Rework.
	public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> getOrCreate(
		idClass: KClass<Id>,
	): RaptorAggregateProjectionLoader<Projection, Id>
}
