package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public interface RaptorAggregateProjectorLoaderManager {

	// FIXME Rework.
	public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> getOrCreate(
		idClass: KClass<Id>,
	): RaptorAggregateProjectionLoader<Projection, Id>
}
