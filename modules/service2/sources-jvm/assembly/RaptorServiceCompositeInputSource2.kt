package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import kotlinx.coroutines.*


internal data class AllInputSource<in Service : RaptorService2, Value>(
	val sources: Set<RaptorServiceInput2<Service, out Value>>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job {
		TODO()
//		val sourceCount = sources.size
//		if (sourceCount == 0) {
//			// No sources, emit immediately
//			// No need for UNDISPATCHED - this doesn't race with external events.
//			coroutineScope.launch {
//				try {
//					handler(service, Unit as Value)
//				}
//				catch (e: CancellationException) {
//					throw e
//				}
//				catch (e: Exception) {
//					errorHandler(RaptorServiceError2(coroutineContext, e))
//				}
//			}
//			return
//		}
//
//		val completedSources = ConcurrentHashMap.newKeySet<RaptorServiceInput2<*>>()
//		val completionDeferred = CompletableDeferred<Unit>()
//
//		// Subscribe to each source
//		for (innerSource in sources) {
//			val innerHandler: suspend Service.(Any?) -> Unit = { _ ->
//				if (completedSources.add(innerSource) && completedSources.size == sourceCount) {
//					completionDeferred.complete(Unit)
//				}
//			}
//			engine.subscribe(scope, service, innerSource as RaptorServiceInput2<Any?>, innerHandler, errorHandler)
//		}
//
//		// Wait for all sources to emit, then call handler
//		// No need for UNDISPATCHED - CompletableDeferred doesn't have timing issues.
//		scope.launch {
//			completionDeferred.await()
//			try {
//				handler(service, Unit as Value)
//			}
//			catch (e: CancellationException) {
//				throw e
//			}
//			catch (e: Exception) {
//				errorHandler(RaptorServiceError2(coroutineContext, e))
//			}
//		}
	}
}


internal data class AnyInputSource<Service : RaptorService2, Value>(
	val sources: Set<RaptorServiceInput2<Service, out Value>>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job {
		TODO()
//		// Subscribe to all sources, forward any emission
//		val innerHandler: suspend Service.(Any?) -> Unit = { value ->
//			handler(this, value as Value)
//		}
//		for (innerSource in sources) {
//			engine.subscribe(scope, service, innerSource as RaptorServiceInput2<Any?>, innerHandler, errorHandler)
//		}
	}
}


@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> onAll(
	vararg changes: RaptorServiceInput2<Service, out Value>,
): RaptorServiceInput2<Service, Value> =
	AllInputSource(changes.toHashSet())

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> onAll(
	changes: Iterable<RaptorServiceInput2<Service, out Value>>,
): RaptorServiceInput2<Service, Value> =
	AllInputSource(changes.toHashSet())

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> onAny(
	vararg changes: RaptorServiceInput2<Service, out Value>,
): RaptorServiceInput2<Service, Value> =
	AnyInputSource(changes.toHashSet())

@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> onAny(
	changes: Iterable<RaptorServiceInput2<Service, out Value>>,
): RaptorServiceInput2<Service, Value> =
	AnyInputSource(changes.toHashSet())
