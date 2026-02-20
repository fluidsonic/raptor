package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import kotlinx.coroutines.*
import org.slf4j.*


/**
 * Plugin that enables RaptorService2 support.
 *
 * This plugin integrates with RaptorLifecyclePlugin to manage v2 services:
 * - Registers service factories with DI during completion
 * - Creates service instances during lifecycle start
 * - Subscribes to input sources and dispatches to handlers
 * - Stops services during lifecycle stop
 */
public object RaptorService2Plugin : RaptorPluginWithConfiguration<RaptorService2PluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorService2PluginConfiguration {
		// Services are registered to the lifecycle component's registry via service2() DSL,
		// so we need to read from there, not from our own componentRegistry.
		// Use configure() to access the lifecycle extension property.
		var serviceRegistrations: List<RaptorServiceRegistration2<RaptorService2>> = emptyList()
		var middlewareRegistrations: List<MiddlewareRegistration> = emptyList()

		configure(RaptorLifecyclePlugin) {
			serviceRegistrations = lifecycle.componentRegistry
				.many(Keys2.servicesComponent)
				.map { it.registration() }

			middlewareRegistrations = lifecycle.componentRegistry
				.oneOrNull(Keys2.middlewareComponent)
				?.registrations
				.orEmpty()
		}

		// Register service factories with DI
		if (serviceRegistrations.isNotEmpty()) {
			configure(RaptorDIPlugin) {
				di {
					// Register the worker provider once (not per-service)
					RaptorServiceRegistration2.installWorkerProvider(this)

					for (registration in serviceRegistrations)
						registration.install(this)
				}
			}
		}

		// Create controllers for each service
		val controllers = serviceRegistrations.map { registration ->
			RaptorServiceController2(
				diKey = registration.diKey,
				errorHandler = registration.errorHandler,
				inputSources = registration.inputSources,
				name = registration.name,
			)
		}

		return RaptorService2PluginConfiguration(
			controllers = controllers,
			middlewareRegistrations = middlewareRegistrations,
		)
	}


	override fun RaptorPluginInstallationScope.install() {
		// Require lifecycle plugin
		require(RaptorLifecyclePlugin)

		// Register lifecycle hooks.
		// Lower priority = runs later in onStart (descending sort), earlier in onStop (ascending sort).
		lifecycle {
			onStart("services2 creation") { createServices2() }
			onStart("services2 middleware", priority = PRIORITY_START_MIDDLEWARE) { startMiddleware2() }
			onStart("services2", priority = PRIORITY_START_SERVICES) { startServices2() }
			onStop("services2", priority = PRIORITY_STOP_SERVICES) { stopServices2() }
		}

		// Register for aggregates loaded notification if domain plugin is available.
		// Must run after startServices2 to ensure services have subscribed first.
		optional(RaptorDomainPlugin) {
			lifecycle {
				onStart("services2 aggregates loaded notification", priority = PRIORITY_NOTIFY_AGGREGATES_LOADED) {
					notifyAggregatesLoaded()
				}
			}
		}
	}


	// Lifecycle hook priorities. Lower value = runs later in onStart, earlier in onStop.
	private const val PRIORITY_START_MIDDLEWARE = Int.MIN_VALUE + 2
	private const val PRIORITY_START_SERVICES = Int.MIN_VALUE + 1
	private const val PRIORITY_NOTIFY_AGGREGATES_LOADED = Int.MIN_VALUE
	private const val PRIORITY_STOP_SERVICES = Int.MAX_VALUE - 1


	override fun toString(): String = "service2"
}


public class RaptorService2PluginConfiguration internal constructor(
	internal val controllers: List<RaptorServiceController2<*>>,
	internal val middlewareRegistrations: List<MiddlewareRegistration>,
) {

	internal var middlewareController: RaptorMiddlewareController? = null
	internal var subscriptionEngine: RaptorServiceSubscriptionEngine? = null
}


private suspend fun RaptorLifecycleStartScope.createServices2() {
	val config = context.plugins.getOrNull(RaptorService2Plugin) ?: return
	if (config.controllers.isEmpty()) return

	val di = context.di
	val logger: Logger = di.get()

	coroutineScope {
		config.controllers.forEach { controller ->
			launch { controller.createIn(di = di, logger = logger) }
		}
	}
}


private fun RaptorLifecycleStartScope.startMiddleware2() {
	val config = context.plugins.getOrNull(RaptorService2Plugin) ?: return
	if (config.middlewareRegistrations.isEmpty()) return

	val di = context.di
	val logger: Logger = di.get()

	// Create middleware controller
	val middlewareController = RaptorMiddlewareController(
		di = di,
		registrations = config.middlewareRegistrations,
	)
	config.middlewareController = middlewareController

	// Create subscription engine with middleware
	val subscriptionEngine = RaptorServiceSubscriptionEngine(
		context = context,
		logger = logger,
		middlewareController = middlewareController,
	)
	config.subscriptionEngine = subscriptionEngine

	// Start middleware subscriptions before services
	subscriptionEngine.startMiddleware(this)

	logger.debug("Started {} stream middleware.", config.middlewareRegistrations.size)
}


private fun RaptorLifecycleStartScope.startServices2() {
	val config = context.plugins.getOrNull(RaptorService2Plugin) ?: return
	if (config.controllers.isEmpty()) return

	val logger: Logger = context.di.get()

	// Use existing subscription engine if middleware created it, otherwise create new one
	val subscriptionEngine = config.subscriptionEngine ?: RaptorServiceSubscriptionEngine(
		context = context,
		logger = logger,
	).also { config.subscriptionEngine = it }

	for (controller in config.controllers)
		controller.start(lifecycleScope = this, subscriptionEngine = subscriptionEngine)
}


private suspend fun RaptorLifecycleStopScope.stopServices2() {
	val config = context.plugins.getOrNull(RaptorService2Plugin) ?: return

	supervisorScope {
		config.controllers.forEach { controller ->
			launch { controller.stop() }
		}
	}
}


private fun RaptorLifecycleStartScope.notifyAggregatesLoaded() {
	val config = context.plugins.getOrNull(RaptorService2Plugin) ?: return
	config.subscriptionEngine?.notifyAggregatesLoaded()
}
