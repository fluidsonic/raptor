package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


internal val RaptorTransactionContext.aggregateManager: RaptorAggregateManager
	get() = properties[Keys.aggregateManagerProperty] ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)


// FIXME rn to aggregateProjectionLoader
@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorTransactionScope.projectionLoader(
	idClass: KClass<Id>,
): RaptorAggregateProjectionLoader<*, *> =
	context.aggregateProjectionLoaderManager.getOrCreate(idClass)


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
