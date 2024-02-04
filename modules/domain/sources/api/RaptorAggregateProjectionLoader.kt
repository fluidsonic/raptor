package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionLoader<out Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> {

	public suspend fun exists(id: Id): Boolean =
		fetchOrNull(id) != null

	public fun fetchAll(): Flow<Projection>

	public suspend fun fetchOrNull(id: Id): Projection?

	public fun fetchOrSkip(ids: Iterable<Id>): Flow<Projection> {
		@Suppress("NAME_SHADOWING")
		val ids = ids.toSet()

		return flow {
			for (id in ids)
				fetchOrNull(id)?.let { emit(it) }
		}
	}
}


@Deprecated(message = "rename", replaceWith = ReplaceWith("fetchAll()"))
public fun <Projection : RaptorAggregateProjection<*>> RaptorAggregateProjectionLoader<Projection, *>.loadAll(): Flow<Projection> =
	fetchAll()


@Deprecated(message = "rename", replaceWith = ReplaceWith("fetchOrNull(id)"))
public suspend fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorAggregateProjectionLoader<Projection, Id>.loadOrNull(
	id: Id,
): Projection? =
	fetchOrNull(id)


@Deprecated(message = "rename", replaceWith = ReplaceWith("fetchOrSkip(ids)"))
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorAggregateProjectionLoader<Projection, Id>.loadOrSkip(
	ids: Iterable<Id>,
): Flow<Projection> =
	fetchOrSkip(ids)
