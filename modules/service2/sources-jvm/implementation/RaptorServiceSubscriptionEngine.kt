package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import org.slf4j.*


internal class RaptorServiceSubscriptionEngine(
	internal val context: RaptorContext,
	internal val logger: Logger,
	aggregateEventSource: RaptorAggregateEventSource? = null,
	aggregateProjectionEventSource: RaptorAggregateProjectionEventSource? = null,
	clock: Clock? = null,
	private val middlewareController: RaptorMiddlewareController? = null,
) {

	internal val aggregateEventSource: RaptorAggregateEventSource? by lazy {
		aggregateEventSource ?: context.di.getOrNull<RaptorAggregateEventSource>()
	}

	internal val aggregateProjectionEventSource: RaptorAggregateProjectionEventSource? by lazy {
		aggregateProjectionEventSource ?: context.di.getOrNull<RaptorAggregateProjectionEventSource>()
	}

	internal val clock: Clock by lazy {
		clock ?: context.di.get()
	}

	@Volatile
	private var aggregatesLoaded = false

	private val aggregatesLoadedListeners = mutableListOf<CompletableDeferred<Unit>>()


	/**
	 * Start middleware subscriptions to aggregate projection events.
	 * This should be called before service subscriptions start.
	 *
	 * Middleware processes all events (including history) to build indexes
	 * before services start processing. Subscribes per aggregate definition
	 * via [RaptorAggregateProjectionEventSource].
	 */
	@Suppress("UNCHECKED_CAST")
	fun startMiddleware(scope: CoroutineScope) {
		val controller = middlewareController ?: return
		val eventSource = checkNotNull(aggregateProjectionEventSource) {
			"No aggregate projection event source available for middleware. Make sure the domain plugin is installed."
		}
		val definitions = context.di.get<RaptorAggregateDefinitions>()

		for (definition in definitions) {
			val projDef = definition.projectionDefinition ?: continue

			subscribeMiddleware(
				controller = controller,
				eventSource = eventSource,
				scope = scope,
				changeClass = definition.changeClass as KClass<Nothing>,
				idClass = definition.idClass as KClass<Nothing>,
				projectionClass = projDef.projectionClass as KClass<Nothing>,
			)
		}
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>>
		subscribeMiddleware(
		controller: RaptorMiddlewareController,
		eventSource: RaptorAggregateProjectionEventSource,
		scope: CoroutineScope,
		changeClass: KClass<Change>,
		idClass: KClass<Id>,
		projectionClass: KClass<Projection>,
	) {
		context(scope) {
			eventSource.subscribe(
				handler = { event ->
					try {
						controller.process(event)
					}
					catch (e: CancellationException) {
						throw e
					}
					catch (e: Exception) {
						logger.error("Error in middleware processing: ${e.message}", e)
						throw e // Middleware errors should stop the lifecycle
					}
				},
				changeClasses = setOf(changeClass),
				idClass = idClass,
				projectionClass = projectionClass,
				replay = true,
			)
		}
	}


	context(scope: CoroutineScope)
	fun <Service : RaptorService2, Value> subscribe(
		service: Service,
		source: RaptorServiceInput2<Service, Value>,
		handler: suspend (Value) -> Unit,
	) {
		when (source) {
			is PersistentQueueInputSource2<*, *, *> ->
				TODO()
//				subscribeToPersistentQueue(scope, service, source, wrapWithErrorHandling(service, handler, errorHandler))

			else ->
				context(service, context) {
					source.subscribe(handler)
				}
		}
	}


	internal fun <Service : RaptorService2, Value> wrapWithErrorHandling(
		service: Service,
		handler: suspend Service.(Value) -> Unit,
		errorHandler: suspend (RaptorService2.Error) -> Unit,
	): suspend Service.(Value) -> Unit = { value ->
		try {
			handler(value)
		}
		catch (e: CancellationException) {
			throw e
		}
		catch (e: Exception) {
			errorHandler(RaptorService2.Error(currentCoroutineContext(), e))
		}
	}


	@Suppress("UNCHECKED_CAST")
	private fun <Service : RaptorService2, Value> subscribeToPersistentQueue(
		scope: CoroutineScope,
		service: Service,
		source: PersistentQueueInputSource2<*, *, *>,
		handler: suspend Service.(Value) -> Unit,
	) {
		// No need for UNDISPATCHED - queue subscription doesn't need to race with events.
		// Regular launch allows proper cancellation handling.
		scope.launch {
			val typedSource = source as PersistentQueueInputSource2<Service, Any, Any>
			typedSource.processQueue(service) { key, value ->
				handler(service, (key to value) as Value)
			}
		}
	}


	internal suspend fun waitForAggregatesLoaded() {
		if (aggregatesLoaded) return

		val deferred = CompletableDeferred<Unit>()
		synchronized(aggregatesLoadedListeners) {
			if (aggregatesLoaded) return
			aggregatesLoadedListeners.add(deferred)
		}
		deferred.await()
	}


	fun notifyAggregatesLoaded() {
		synchronized(aggregatesLoadedListeners) {
			aggregatesLoaded = true
			aggregatesLoadedListeners.forEach { it.complete(Unit) }
			aggregatesLoadedListeners.clear()
		}
	}
}
