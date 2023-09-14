package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


// TODO Add a dependency system for executing actions in the right order instead of using priorities.
// TODO Replace star/stop actions with full observers. Properly call stop/failure callbacks if startup/stop fails.
public class RaptorLifecycleComponent internal constructor() : RaptorComponent.Base<RaptorLifecycleComponent>(RaptorLifecyclePlugin) {

	private val startActions: MutableList<LifecycleAction<RaptorLifecycleStartScope>> = mutableListOf()
	private val stopActions: MutableList<LifecycleAction<RaptorLifecycleStopScope>> = mutableListOf()


	@RaptorDsl
	public fun onStart(
		label: String,
		priority: Int = 0,
		action: suspend RaptorLifecycleStartScope.() -> Unit,
	) {
		startActions += LifecycleAction(block = action, label = label, priority = priority)
	}


	@RaptorDsl
	public fun onStop(
		label: String,
		priority: Int = 0,
		action: suspend RaptorLifecycleStopScope.() -> Unit,
	) {
		stopActions += LifecycleAction(block = action, label = label, priority = priority)
	}


	internal fun <Service : RaptorService> service(
		name: String,
		factory: RaptorDI.() -> Service,
	): RaptorServiceComponent<Service> =
		componentRegistry.register(Keys.serviceComponent, RaptorServiceComponent(factory = factory, name = name))


	internal fun serviceRegistrations(): Collection<RaptorServiceRegistration<*>> =
		componentRegistry.many(Keys.serviceComponent).map { it.registration() }


	override fun RaptorComponentConfigurationEndScope<RaptorLifecycleComponent>.onConfigurationEnded() {
		propertyRegistry.register(Keys.lifecycleProperty, DefaultLifecycle(
			context = lazyContext,
			startActions = startActions.toList(),
			stopActions = stopActions.toList()
		))
	}


	override fun toString(): String = "lifecycle"
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStart(
	label: String,
	priority: Int = 0,
	action: suspend RaptorLifecycleStartScope.() -> Unit,
) {
	this {
		onStart(priority = priority, label = label, action = action)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStop(
	label: String,
	priority: Int = 0,
	action: suspend RaptorLifecycleStopScope.() -> Unit,
) {
	this {
		onStop(priority = priority, label = label, action = action)
	}
}
