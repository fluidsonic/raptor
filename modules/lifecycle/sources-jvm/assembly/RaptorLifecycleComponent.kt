package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


// TODO Add a dependency system for executing actions in the right order instead of using priorities.
// TODO Replace star/stop actions with full observers. Properly call stop/failure callbacks if startup/stop fails.
public class RaptorLifecycleComponent internal constructor() : RaptorComponent.Base<RaptorLifecycleComponent>(RaptorLifecyclePlugin) {

	private val serviceRegistrations: MutableList<ServiceRegistration> = mutableListOf()
	private val startActions: MutableList<LifecycleAction<RaptorLifecycleStartScope>> = mutableListOf()
	private val stopActions: MutableList<LifecycleAction<RaptorLifecycleStopScope>> = mutableListOf()


	@RaptorDsl
	public fun onStart(
		priority: Int = 0,
		action: suspend RaptorLifecycleStartScope.() -> Unit,
	) {
		startActions += LifecycleAction(block = action, priority = priority)
	}


	@RaptorDsl
	public fun onStop(
		priority: Int = 0,
		action: suspend RaptorLifecycleStopScope.() -> Unit,
	) {
		stopActions += LifecycleAction(block = action, priority = priority)
	}


	internal fun service(name: String, factory: RaptorDI.() -> RaptorService) {
		serviceRegistrations += ServiceRegistration(factory = factory, name = name)
	}


	internal fun serviceRegistrations() =
		serviceRegistrations.toList()


	override fun RaptorComponentConfigurationEndScope<RaptorLifecycleComponent>.onConfigurationEnded() {
		propertyRegistry.register(Keys.lifecycleProperty, DefaultLifecycle(
			context = lazyContext,
			startActions = startActions.toList(),
			stopActions = stopActions.toList()
		))
	}


	override fun toString(): String = "lifecycle"


	internal class ServiceRegistration(
		val factory: RaptorDI.() -> RaptorService,
		val name: String,
	) {

		val diKey = ServiceDIKey(name)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStart(
	priority: Int = 0,
	action: suspend RaptorLifecycleStartScope.() -> Unit,
) {
	this {
		onStart(priority = priority, action = action)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStop(
	priority: Int = 0,
	action: suspend RaptorLifecycleStopScope.() -> Unit,
) {
	this {
		onStop(priority = priority, action = action)
	}
}
