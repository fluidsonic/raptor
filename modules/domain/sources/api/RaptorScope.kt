package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


// FIXME remove, per-tx only

@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorScope.projectionLoader(
	idClass: KClass<Id>,
): RaptorAggregateProjectionLoader<Projection, Id> =
	context.plugins.domain.aggregates.projectionLoaderManager.getOrCreate(idClass)


@RaptorDsl
@Suppress("UNCHECKED_CAST")
public inline fun <Projection : RaptorAggregateProjection<Id>, reified Id : RaptorAggregateProjectionId>
	RaptorScope.projectionLoader(): RaptorAggregateProjectionLoader<Projection, Id> =
	projectionLoader(Id::class) as RaptorAggregateProjectionLoader<Projection, Id>


@RaptorDsl
public inline fun <Projection : RaptorAggregateProjection<Id>, reified Id : RaptorAggregateProjectionId> RaptorScope.projectionLoader(
	@Suppress("UNUSED_PARAMETER") type: RaptorProjectionType<Projection, Id>,
): RaptorAggregateProjectionLoader<Projection, Id> =
	projectionLoader()
