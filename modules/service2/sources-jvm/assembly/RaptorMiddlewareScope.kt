package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.reflect.*


/**
 * DSL scope for registering stream middleware.
 *
 * Example:
 * ```kotlin
 * middleware {
 *     middleware<AdminIndexMiddleware>()
 *     middleware<AccountIndexMiddleware>()
 * }
 * ```
 */
@RaptorDsl
public class RaptorMiddlewareScope internal constructor() {

	internal val registrations = mutableListOf<MiddlewareRegistration>()


	/**
	 * Register a middleware class to be instantiated via DI.
	 */
	public inline fun <reified T : RaptorStreamMiddleware> middleware() {
		middleware(T::class)
	}


	/**
	 * Register a middleware class to be instantiated via DI.
	 */
	public fun <T : RaptorStreamMiddleware> middleware(type: KClass<T>) {
		registrations += MiddlewareRegistration(type)
	}
}


internal data class MiddlewareRegistration(
	val type: KClass<out RaptorStreamMiddleware>,
)


/**
 * Component that collects middleware registrations from plugins.
 */
internal class RaptorMiddlewareComponent : RaptorComponent.Base<RaptorMiddlewareComponent>(RaptorLifecyclePlugin) {

	internal val registrations = mutableListOf<MiddlewareRegistration>()


	fun addRegistrations(newRegistrations: List<MiddlewareRegistration>) {
		registrations += newRegistrations
	}
}


/**
 * Register stream middleware for aggregate event processing.
 *
 * Middleware processes events synchronously before service handlers,
 * making them ideal for building indexes that services query.
 *
 * Example:
 * ```kotlin
 * override fun RaptorPluginInstallationScope.install() {
 *     di.provide<AdminIndex>(::AdminIndexMiddleware)
 *
 *     middleware {
 *         middleware<AdminIndexMiddleware>()
 *     }
 * }
 * ```
 */
@RaptorDsl
public fun RaptorPluginScope<in RaptorLifecyclePlugin>.middleware(
	configure: RaptorMiddlewareScope.() -> Unit,
) {
	val scope = RaptorMiddlewareScope().apply(configure)
	if (scope.registrations.isEmpty()) return

	// Get or create the middleware component
	val component = lifecycle.componentRegistry.oneOrNull(Keys2.middlewareComponent)
		?: RaptorMiddlewareComponent().also {
			lifecycle.componentRegistry.register(Keys2.middlewareComponent, it)
		}

	component.addRegistrations(scope.registrations)
}
