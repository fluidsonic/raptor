package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


@RaptorDsl
public suspend fun <Id : RaptorAggregateId> RaptorTransactionScope.execute(id: Id, command: RaptorAggregateCommand<Id>) {
	context.plugins.domain.aggregates.manager.execute(id, command)
}


// FIXME rm
@RaptorDsl
public suspend fun RaptorTransactionScope.commit() {
	context.plugins.domain.aggregates.manager.commit()
}


// FIXME per-tx only, rename to aggregateProjectionLoader
@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorTransactionScope.projectionLoader(
	idClass: KClass<Id>,
): RaptorAggregateProjectionLoader<Projection, Id> =
	context.plugins.domain.aggregates.projectionLoaderManager.getOrCreate(idClass)


@RaptorDsl
@Suppress("UNCHECKED_CAST")
public inline fun <Projection : RaptorAggregateProjection<Id>, reified Id : RaptorAggregateProjectionId>
	RaptorTransactionScope.projectionLoader(): RaptorAggregateProjectionLoader<Projection, Id> =
	projectionLoader(Id::class) as RaptorAggregateProjectionLoader<Projection, Id>


@RaptorDsl
public inline fun <Projection : RaptorAggregateProjection<Id>, reified Id : RaptorAggregateProjectionId> RaptorTransactionScope.projectionLoader(
	@Suppress("UNUSED_PARAMETER") type: RaptorProjectionType<Projection, Id>,
): RaptorAggregateProjectionLoader<Projection, Id> =
	projectionLoader()
