package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.event.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import kotlin.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.datetime.Clock


internal data class BatchByInputSource<in Service : RaptorService2, Value>(
	val source: RaptorServiceInput2<Service, Value>,
	val batchKeySelector: suspend context(Service) (Value) -> Any?,
	val batchTimeout: Duration,
) : RaptorServiceInput2<Service, List<Value>> {

	private class BatchState {
		val events: MutableList<Any?> = mutableListOf()
		var timerJob: Job? = null
		var version: Int = 0
	}


	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (List<Value>) -> Unit): Job {
		TODO()
//		val keySelector = batchKeySelector as suspend (Any?) -> Any?
//		val timeout = batchTimeout
//		val lock = Any()
//		val batches = mutableMapOf<Any?, BatchState>()
//
//		val innerHandler: suspend Service.(Any?) -> Unit = { value ->
//			val key = keySelector(value)
//
//			val currentVersion: Int
//
//			synchronized(lock) {
//				val state = batches.getOrPut(key) { BatchState() }
//				state.events.add(value)
//				state.timerJob?.cancel()
//				currentVersion = ++state.version
//
//				state.timerJob = coroutineScope.launch {
//					delay(timeout)
//					val eventsToEmit = synchronized(lock) {
//						if (batches[key] === state && state.version == currentVersion) {
//							batches.remove(key)
//							state.events.toList()
//						}
//						else {
//							emptyList()
//						}
//					}
//					if (eventsToEmit.isNotEmpty()) {
//						try {
//							handler(service, eventsToEmit as List<Value>)
//						}
//						catch (e: CancellationException) {
//							throw e
//						}
//						catch (e: Exception) {
//							errorHandler(RaptorService2.Error(coroutineContext, e))
//						}
//					}
//				}
//			}
//		}
//
//		engine.subscribe(scope, service, source as RaptorServiceInput2<Any?>, innerHandler, errorHandler)
	}
}


internal data class DailyScheduleInputSource<in Service : RaptorService2>(
	val time: LocalTime,
	val timeZone: TimeZone,
) : RaptorServiceInput2<Service, LocalDate> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (LocalDate) -> Unit): Job =
		coroutineScope.launch {
			context.di.get<Clock>() // FIXME
				.dailyFlow(time, timeZone)
				.collect { date ->
					// FIXME service dispatch
					handler(date)
				}
		}
}


internal data class DefaultEventInputSource<in Service : RaptorService2, out Event : RaptorEvent>(
	val event: KClass<out Event>,
) : RaptorServiceInput2<Service, Event> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Event) -> Unit): Job =
		context.eventSource.subscribe(
			handler = handler,
			events = setOf(event), // fIXME
		)
}


internal data class DelayInputSource<in Service : RaptorService2, out Value>(
	val duration: Duration,
	val source: RaptorServiceInput2<Service, Value>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job =
		when {
			duration > Duration.ZERO ->
				source.subscribe { value ->
					coroutineScope.launch {
						kotlinx.coroutines.delay(duration)

						// FIXME dispatch on service
						handler(value)
					}
				}

			else -> source.subscribe(handler)
		}
}


internal data class DelayUntilInputSource<in Service : RaptorService2, Value>(
	val source: RaptorServiceInput2<Service, Value>,
	val timestampSelector: suspend context(Service) (Value) -> Timestamp,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job =
		source.subscribe { value ->
			coroutineScope.launch {
				val now = context.di.get<Clock>().now() // FIXME
				val target = timestampSelector(value)

				if (now < target)
					kotlinx.coroutines.delay(target - now)

				// FIXME dispatch on service
				handler(value)
			}
		}
}


internal data class FilteredInputSource<in Service : RaptorService2, Value>(
	val predicate: suspend context(Service) (Value) -> Boolean,
	val source: RaptorServiceInput2<Service, Value>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit) =
		source.subscribe { if (predicate(it)) handler(it) }
}


internal data class FlatMapFlowInputSource<in Service : RaptorService2, Value, out TransformedValue>(
	val source: RaptorServiceInput2<Service, Value>,
	val transform: suspend context(Service) (Value) -> Flow<TransformedValue>,
) : RaptorServiceInput2<Service, TransformedValue> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (TransformedValue) -> Unit) =
		source.subscribe { value -> transform(value).collect { handler(it) } }
}


internal data class FlatMapIterableInputSource<in Service : RaptorService2, Value, out TransformedValue>(
	val source: RaptorServiceInput2<Service, Value>,
	val transform: suspend context(Service) (Value) -> Iterable<TransformedValue>,
) : RaptorServiceInput2<Service, TransformedValue> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (TransformedValue) -> Unit): Job =
		source.subscribe { value -> transform(value).forEach { handler(it) } }
}


internal data class MapNotNullInputSource<in Service : RaptorService2, Value, out TransformedValue : Any>(
	val source: RaptorServiceInput2<Service, Value>,
	val transform: suspend context(Service) (Value) -> TransformedValue?,
) : RaptorServiceInput2<Service, TransformedValue> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (TransformedValue) -> Unit) =
		source.subscribe { value -> transform(value)?.let { handler(it) } }
}


internal data class QueueInputSource<in Service : RaptorService2, Key : Any, Value : Any, Queue>(
	val queueProperty: KProperty1<in Service, Queue>,
) : RaptorServiceInput2<Service, Pair<Key, Value>> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Pair<Key, Value>) -> Unit): Job {
		TODO("Queue subscription not yet implemented.")
	}
}


internal data class ScheduledTaskInputSource<in Service : RaptorService2, Key : Any, Data : Any>(
	val dataType: KType,
	val keyType: KType,
) : RaptorServiceInput2<Service, Pair<Key, Data>> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Pair<Key, Data>) -> Unit): Job {
		TODO("Scheduled task subscription not yet implemented.")
	}
}


internal data object StartInputSource : RaptorServiceInput2<RaptorService2, Unit> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: RaptorService2)
	override fun subscribe(handler: suspend (Unit) -> Unit): Job =
		// FIXME use service dispatcher
		coroutineScope.launch {
			handler(Unit)
		}
}


internal data class TransformedInputSource<in Service : RaptorService2, Value, out TransformedValue>(
	val source: RaptorServiceInput2<Service, Value>,
	val transform: suspend context(Service) (Value) -> TransformedValue,
) : RaptorServiceInput2<Service, TransformedValue> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (TransformedValue) -> Unit): Job =
		source.subscribe { handler(transform(it)) }
}


// FIXME terminate immediately vs graceful
internal data class WaitingInputSource<in Service : RaptorService2, Value>(
	val source: RaptorServiceInput2<Service, Value>,
	val sourceToWaitFor: RaptorServiceInput2<*, *>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job {
		TODO()
//		val waitDeferred = CompletableDeferred<Unit>()
//
//		// Subscribe to the source we're waiting for in a child scope so it can be cancelled
//		// after the deferred completes, avoiding a subscription leak.
//		val waitScope = CoroutineScope(scope.coroutineContext + Job(parent = scope.coroutineContext.job))
//		val waitHandler: suspend Service.(Any?) -> Unit = { _ ->
//			waitDeferred.complete(Unit)
//			waitScope.cancel()
//		}
//		engine.subscribe(waitScope, service, sourceToWaitFor as RaptorServiceInput2<Any?>, waitHandler, errorHandler)
//
//		// Subscribe to the main source, waiting first
//		val innerHandler: suspend Service.(Any?) -> Unit = { value ->
//			waitDeferred.await()
//			handler(this, value as Value)
//		}
//		engine.subscribe(scope, service, source as RaptorServiceInput2<Any?>, innerHandler, errorHandler)
	}
}


// FIXME
private fun Clock.dailyFlow(
	time: LocalTime,
	timeZone: TimeZone,
): Flow<LocalDate> =
	flow {
		while (true) {
			val now = now()
			val nowDateTime = now.toLocalDateTime(timeZone)
			val todayAtTime = nowDateTime.date.atTime(time)
			val nextDateTime = when {
				todayAtTime > nowDateTime -> todayAtTime
				else -> todayAtTime.date.plus(1, DateTimeUnit.DAY).atTime(time)
			}
			val next = nextDateTime.toTimestamp(timeZone)

			kotlinx.coroutines.delay(next - now)
			emit(nextDateTime.date)
		}
	}
