package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateChangesInputSource<Service : RaptorService2, Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> :
	RaptorServiceInput2<Service, RaptorAggregateEvent<Id, Change>> {

	@RaptorDsl
	public fun includingHistory(): RaptorAggregateChangesInputSource<Service, Id, Change>
}


@PublishedApi
internal data class DefaultAggregateChangesInputSource<Service : RaptorService2, Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	val changes: Set<KClass<out Change>>,
	val idClass: KClass<Id>,
	val includesHistory: Boolean = false,
) : RaptorAggregateChangesInputSource<Service, Id, Change> {

	override fun includingHistory() =
		when (includesHistory) {
			true -> this
			false -> copy(includesHistory = true)
		}


	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (RaptorAggregateEvent<Id, Change>) -> Unit): Job =
		context.aggregateEventSource.subscribe(
			handler = handler,
			changeClasses = changes,
			idClass = idClass,
			replay = includesHistory,
		)
}


public interface RaptorAggregateProjectionChangesInputSource<
	Service : RaptorService2,
	Id : RaptorAggregateProjectionId,
	Change : RaptorAggregateChange<Id>,
	Projection : RaptorAggregateProjection<Id>,
	> :
	RaptorServiceInput2<Service, RaptorAggregateProjectionEvent<Id, Projection, Change>> {

	@RaptorDsl
	public fun includingHistory(): RaptorAggregateProjectionChangesInputSource<Service, Id, Change, Projection>
}


@PublishedApi
internal data class AggregateProjectionChangesInputSource<
	Service : RaptorService2,
	Id : RaptorAggregateProjectionId,
	Change : RaptorAggregateChange<Id>,
	Projection : RaptorAggregateProjection<Id>,
	>(
	val changes: Set<KClass<out Change>>,
	val idClass: KClass<Id>,
	val includesHistory: Boolean = false,
	val projection: KClass<Projection>,
) : RaptorAggregateProjectionChangesInputSource<Service, Id, Change, Projection> {

	override fun includingHistory() =
		when (includesHistory) {
			true -> this
			false -> copy(includesHistory = true)
		}


	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit): Job =
		context.aggregateProjectionEventSource.subscribe(
			handler = handler,
			changeClasses = changes,
			idClass = idClass,
			projectionClass = projection,
			replay = includesHistory,
		)
}


internal data object DefaultAggregatesLoadedInputSource : RaptorServiceInput2<RaptorService2, Unit> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: RaptorService2)
	override fun subscribe(handler: suspend (Unit) -> Unit): Job {
		TODO()
//		val wrappedHandler = engine.wrapWithErrorHandling(service, handler, errorHandler)
//		// No need for UNDISPATCHED here - waitForAggregatesLoaded uses a CompletableDeferred
//		// which doesn't suffer from the same timing issues as SharedFlow subscriptions.
//		scope.launch {
//			engine.waitForAggregatesLoaded()
//
//			wrappedHandler(service, Unit)
//		}
	}
}


@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>> onAggregateChanges(
	vararg changes: KClass<out Change>,
): RaptorAggregateChangesInputSource<Service, Id, Change> =
	DefaultAggregateChangesInputSource(changes = changes.toHashSet(), idClass = Id::class)

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>> onAggregateChanges(
	changes: Iterable<KClass<out Change>>,
): RaptorAggregateChangesInputSource<Service, Id, Change> =
	DefaultAggregateChangesInputSource(changes = changes.toHashSet(), idClass = Id::class)

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>>
	onAggregateProjectionChanges(
	projection: KClass<Projection>,
	vararg changes: KClass<out Change>,
): RaptorAggregateProjectionChangesInputSource<Service, Id, Change, Projection> =
	AggregateProjectionChangesInputSource(changes = changes.toHashSet(), idClass = Id::class, projection = projection)

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>>
	onAggregateProjectionChanges(
	projection: KClass<Projection>,
	changes: Iterable<KClass<out Change>>,
): RaptorAggregateProjectionChangesInputSource<Service, Id, Change, Projection> =
	AggregateProjectionChangesInputSource(changes = changes.toHashSet(), idClass = Id::class, projection = projection)

@RaptorDsl
context(_: RaptorServiceComponent2<*>)
public fun onAggregatesLoaded(): RaptorServiceInput2<RaptorService2, Unit> =
	DefaultAggregatesLoadedInputSource
