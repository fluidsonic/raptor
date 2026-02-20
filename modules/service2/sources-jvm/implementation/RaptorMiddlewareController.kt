package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import kotlin.reflect.full.*


/**
 * Controls middleware lifecycle and event dispatching.
 *
 * Middleware instances are resolved from DI lazily on first use.
 * Events are processed through all middleware in registration order.
 */
internal class RaptorMiddlewareController(
	di: RaptorDI,
	registrations: List<MiddlewareRegistration>,
) {

	/**
	 * Middleware instances, created once from DI.
	 */
	private val middleware: List<RaptorStreamMiddleware> = registrations.map { registration ->
		di.get(registration.type.starProjectedType)
	}


	/**
	 * Process an event through all registered middleware.
	 *
	 * Called synchronously before service handlers see the event.
	 */
	suspend fun process(event: RaptorAggregateProjectionEvent<*, *, *>) {
		for (m in middleware) {
			m.intercept(event)
		}
	}
}
