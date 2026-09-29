package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.contracts.*
import kotlin.reflect.*
import kotlinx.coroutines.flow.*


@RaptorDsl
public val RaptorScope.aggregateStream: RaptorAggregateStream
	get() = di.get()


@RaptorDsl
public val RaptorScope.aggregateProjectionStream: RaptorAggregateProjectionStream
	get() = di.get()


@RaptorDsl
public val RaptorScope.aggregateProvider: RaptorAggregateProvider
	get() = di.get()


@RaptorDsl
public val RaptorScope.aggregateStore: RaptorAggregateStore
	get() = di.get()


/**
 * Loads one page of aggregate events ordered by event ID (newest first by default), filtered by
 * aggregate ID class and change class. See [RaptorAggregateLoader.loadPage] for paging semantics.
 *
 * [changes] maps an aggregate's ID class to the change classes to include; a `null` value includes all
 * changes of that aggregate and a `null` map disables filtering. Change classes are matched exactly,
 * so a nested sealed leaf must be listed itself.
 *
 * @throws IllegalArgumentException if an ID class is not a registered aggregate, or a change class is
 *   not registered for its aggregate.
 * @throws IllegalStateException if an aggregate is individual.
 */
@RaptorDsl
@Suppress("UNCHECKED_CAST")
public fun RaptorScope.loadAggregateEvents(
	limit: Int,
	changes: Map<KClass<out RaptorAggregateId>, Set<KClass<out RaptorAggregateChange<*>>>?>? = null,
	before: RaptorAggregateEventId? = null,
	after: RaptorAggregateEventId? = null,
	descending: Boolean = true,
): Flow<RaptorAggregateEvent<*, *>> {
	val definitions = di.get<RaptorAggregateDefinitions>()

	val resolvedChanges = changes?.entries?.associate<
		_, RaptorAggregateDefinition<*, *, *, *>, Set<RaptorAggregateChangeDefinition<*, *>>?
		> { (idClass, changeClasses) ->
		val definition = requireNotNull(definitions[idClass]) {
			"`${idClass.qualifiedName}` is not the ID class of a registered aggregate."
		} as RaptorAggregateDefinition<*, RaptorAggregateId, *, *>

		val changeDefinitions = changeClasses?.mapTo(hashSetOf<RaptorAggregateChangeDefinition<*, *>>()) { changeClass ->
			requireNotNull(definition.changeDefinition(changeClass as KClass<out RaptorAggregateChange<RaptorAggregateId>>)) {
				"`${changeClass.qualifiedName}` is not a registered change of aggregate '${definition.discriminator}'."
			}
		}

		definition to changeDefinitions
	}

	return aggregateStore.loadPage(
		limit = limit,
		changes = resolvedChanges,
		before = before,
		after = after,
		descending = descending,
	)
}


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
