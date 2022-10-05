package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionLoader<out Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> {

	public suspend fun exists(id: Id): Boolean =
		loadOrNull(id) != null

	public fun loadAll(): Flow<Projection>

	public suspend fun loadOrNull(id: Id): Projection?
}
