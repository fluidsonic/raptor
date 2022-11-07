package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionLoader<out Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> {

	public suspend fun exists(id: Id): Boolean =
		loadOrNull(id) != null

	public fun loadAll(): Flow<Projection>

	public suspend fun loadOrNull(id: Id): Projection?

	public fun loadOrSkip(ids: Iterable<Id>): Flow<Projection> {
		@Suppress("NAME_SHADOWING")
		val ids = ids.toSet()

		return flow {
			for (id in ids)
				loadOrNull(id)?.let { emit(it) }
		}
	}
}
