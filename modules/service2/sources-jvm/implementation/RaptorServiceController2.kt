package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.di.*
import kotlin.coroutines.*
import kotlinx.coroutines.*
import org.slf4j.*


internal class RaptorServiceController2<Service : RaptorService2>(
	private val diKey: ServiceDIKey2<Service>,
	private val errorHandler: RaptorServiceComponent2.ErrorHandler,
	private val inputSources: List<RaptorServiceInputRegistration<Service, *>>,
	private val name: String,
) {

	private var _service: Service? = null
	private var _worker: DefaultRaptorServiceWorker? = null
	private var lifecycleScope: CoroutineScope? = null
	private var serviceScope: CoroutineScope? = null
	private var logger: Logger? = null

	val service: Service
		get() = checkNotNull(_service) { "Service '$name' has not been created yet." }


	suspend fun createIn(di: RaptorDI, logger: Logger) {
		check(_service == null) { "Service '$name' has already been created." }

		this.logger = logger

		// Note: We don't create the service scope here. It will be created during start()
		// when we have access to the lifecycle's scope. This allows createIn() to complete
		// without blocking the coroutineScope that creates all services in parallel.

		// Create a temporary worker for service construction (with a placeholder scope)
		// The worker's scope will be replaced during start()
		val placeholderScope = CoroutineScope(EmptyCoroutineContext)
		val worker = DefaultRaptorServiceWorker(placeholderScope)
		_worker = worker

		// Create the service, with worker available via DI
		val service = withContext(CurrentServiceWorker.asContextElement(worker)) {
			di.get(diKey)
		}
		_service = service

		logger.debug("Created service '$name'.")
	}


	fun start(lifecycleScope: CoroutineScope, subscriptionEngine: RaptorServiceSubscriptionEngine) {
		val service = checkNotNull(_service) { "Service '$name' has not been created yet." }

		this.lifecycleScope = lifecycleScope

		// Create the actual service scope now that we have the lifecycle scope
		val scope = CoroutineScope(
			lifecycleScope.coroutineContext +
				SupervisorJob(parent = lifecycleScope.coroutineContext.job) +
				CoroutineName("RaptorService2: $name")
		)
		serviceScope = scope

		// Replace the worker's placeholder scope with the actual service scope
		_worker?.replaceScope(scope)

		context(scope) {
			// Subscribe to all input sources with error handling
			for (registration in inputSources) {
				@Suppress("UNCHECKED_CAST")
				subscriptionEngine.subscribe(
					service = service,
					source = registration.source,
					handler = registration.handler as suspend (Any?) -> Unit,
				)
			}
		}
	}


	private suspend fun handleError(error: RaptorService2.Error) {
		when (errorHandler) {
			is RaptorServiceComponent2.ErrorHandler.Default -> {
				// Default: log and continue
				logger?.error("Error in service '$name': ${error.throwable.message}", error.throwable)
			}

			is RaptorServiceComponent2.ErrorHandler.StopService -> {
				logger?.error("Error in service '$name', stopping service: ${error.throwable.message}", error.throwable)
				serviceScope?.cancel(CancellationException("Service '$name' stopped due to error.", error.throwable))
			}

			is RaptorServiceComponent2.ErrorHandler.StopLifecycle -> {
				logger?.error("Error in service '$name', stopping lifecycle: ${error.throwable.message}", error.throwable)
				lifecycleScope?.cancel(CancellationException("Lifecycle stopped due to error in service '$name'.", error.throwable))
			}

			is RaptorServiceComponent2.ErrorHandler.Custom -> {
				errorHandler.handler(error)
			}
		}
	}


	suspend fun stop() {
		try {
			serviceScope?.cancel(CancellationException("Service '$name' is stopping."))
			// Wait for all child jobs to complete
			serviceScope?.coroutineContext?.job?.children?.forEach { it.join() }
		}
		finally {
			_service = null
			lifecycleScope = null
			serviceScope = null
		}
	}


	override fun toString(): String =
		"RaptorServiceController2 '$name'"
}
