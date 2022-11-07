package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.contracts.*
import kotlin.reflect.*


@RaptorDsl
public val RaptorScope.aggregateStream: RaptorAggregateStream
	get() = di.get()


@RaptorDsl
public val RaptorScope.aggregateProjectionStream: RaptorAggregateProjectionStream
	get() = di.get()


@RaptorDsl
public val RaptorScope.aggregateStore: RaptorAggregateStore
	get() = di.get()


@RaptorDsl
public val RaptorScope.commandExecutor: RaptorAggregateCommandExecutor
	get() = di.get()


@RaptorDsl
public suspend fun <Id : RaptorAggregateId> RaptorScope.execute(id: Id, command: RaptorAggregateCommand<Id>) {
	execute(id = id, version = null, command = command)
}


@RaptorDsl
public suspend fun <Id : RaptorAggregateId> RaptorScope.execute(id: Id, version: Int?, command: RaptorAggregateCommand<Id>) {
	commandExecutor.execute(id = id, command = command, version = version)
}


@RaptorDsl
public suspend inline fun <Result> RaptorScope.execution(
	retryOnVersionConflict: Boolean = false,
	action: RaptorAggregateCommandExecution.() -> Result,
): Result {
	contract {
		callsInPlace(action, InvocationKind.AT_LEAST_ONCE)
	}

	return commandExecutor.execution(retryOnVersionConflict = retryOnVersionConflict, action = action)
}


@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> RaptorScope.projectionLoader(
	idClass: KClass<Id>,
): RaptorAggregateProjectionLoader<Projection, Id> =
	di.get<RaptorAggregateProjectionLoaderManager>().getOrCreate(idClass)


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
