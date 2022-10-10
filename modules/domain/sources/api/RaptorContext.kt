package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


// FIXME tx sub managers
internal val RaptorContext.aggregateManager: DefaultAggregateManager
	get() = properties[Keys.aggregateManagerProperty] ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)


// FIXME tx sub managers
internal val RaptorContext.aggregateProjectionLoaderManager: RaptorAggregateProjectorLoaderManager
	get() = properties[Keys.aggregateProjectorLoaderManagerProperty] ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)


public val RaptorContext.domain: RaptorDomain
	get() = properties[Keys.domainProperty] ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)


// FIXME remove, per-tx only vvvvvv
@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorScope.projectionLoader(
	idClass: KClass<Id>,
): RaptorAggregateProjectionLoader<*, *> =
	context.aggregateProjectionLoaderManager.getOrCreate(idClass)


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
