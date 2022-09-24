package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import kotlinx.coroutines.flow.*


public interface RaptorProjectionLoader<out Projection : RaptorProjection<Id>, Id : RaptorProjectionId> {

	public suspend fun exists(id: Id): Boolean =
		loadOrNull(id) != null

	public fun loadAll(): Flow<Projection>

	public suspend fun loadOrNull(id: Id): Projection?
}


@RaptorDsl
public inline fun <reified Projection : RaptorProjection<Id>, reified Id : RaptorProjectionId>
	RaptorTransactionScope.projectionLoader(@Suppress("UNUSED_PARAMETER") type: RaptorProjectionType<Projection, Id>): RaptorProjectionLoader<Projection, Id> =
	di.get()
