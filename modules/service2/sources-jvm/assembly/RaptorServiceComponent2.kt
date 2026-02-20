package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import kotlinx.coroutines.*


public class RaptorServiceComponent2<Service : RaptorService2> internal constructor(
	private val name: String,
) : RaptorComponent.Base<RaptorServiceComponent2<Service>>(RaptorLifecyclePlugin) {

	private var _factory: (RaptorDI.() -> Service)? = null
	private val inputSources: MutableList<RaptorServiceInputRegistration<Service, *>> = mutableListOf()
	internal var errorHandler: ErrorHandler = ErrorHandler.Default


	internal fun registration(): RaptorServiceRegistration2<Service> {
		val factory = checkNotNull(_factory) { "Service '$name' has no factory. Use factory { ... } to define one." }
		return RaptorServiceRegistration2(
			errorHandler = errorHandler,
			factory = factory,
			inputSources = inputSources.toList(),
			name = name,
		)
	}


	internal fun setFactory(factory: RaptorDI.() -> Service) {
		check(_factory == null) { "Service '$name' already has a factory defined." }
		_factory = factory
	}


	internal fun <Value> addInputSource(source: RaptorServiceInput2<Service, Value>, handler: suspend Service.(Value) -> Unit) {
		TODO()
//		inputSources += RaptorServiceInputRegistration(source, handler)
	}


	override fun toString(): String = "service '$name'"


	internal sealed interface ErrorHandler {

		data object Default : ErrorHandler

		data object StopService : ErrorHandler

		data object StopLifecycle : ErrorHandler

		data class Custom(val handler: suspend (RaptorService2.Error) -> Unit) : ErrorHandler
	}
}


internal data class RaptorServiceInputRegistration<Service : RaptorService2, Value>(
	val source: RaptorServiceInput2<Service, Value>,
	val handler: suspend (Value) -> Unit,
)


@RaptorDsl
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2> factory(factory: context(RaptorDI) () -> Service) {
	component.setFactory { factory(this) }
}


@RaptorDsl
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2> onError(): RaptorServiceInput2<Service, RaptorService2.Error> =
	ErrorInputSource(component)


@RaptorDsl
public fun RaptorServiceInput2<*, RaptorService2.Error>.log() {
	when (this) {
		is ErrorInputSource<*> -> component.errorHandler = RaptorServiceComponent2.ErrorHandler.Default
	}
}


@RaptorDsl
public fun RaptorServiceInput2<*, RaptorService2.Error>.stopService() {
	when (this) {
		is ErrorInputSource<*> -> component.errorHandler = RaptorServiceComponent2.ErrorHandler.StopService
	}
}


@RaptorDsl
public fun RaptorServiceInput2<*, RaptorService2.Error>.stopLifecycle() {
	when (this) {
		is ErrorInputSource<*> -> component.errorHandler = RaptorServiceComponent2.ErrorHandler.StopLifecycle
	}
}


/**
 * Error handling is not implemented via subscription. Instead, calling [onError] returns this source,
 * and the terminal methods ([log], [stopService], [stopLifecycle]) set the error handler as a
 * side-effect on the [RaptorServiceComponent2].
 *
 * [subscribeTo] throws because `onError().handle { ... }` is not supported -- use the dedicated
 * terminal methods instead.
 */
internal data class ErrorInputSource<Service : RaptorService2>(
	val component: RaptorServiceComponent2<Service>,
) : RaptorServiceInput2<Service, RaptorService2.Error> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (RaptorService2.Error) -> Unit): Job {
		throw UnsupportedOperationException(
			"onError().handle { ... } is not supported. Use onError().log(), onError().stopService(), or onError().stopLifecycle() instead."
		)
	}
}
