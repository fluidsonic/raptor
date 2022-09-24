package io.fluidsonic.raptor


public class RaptorLifecycleComponent internal constructor() : RaptorComponent.Base<RaptorLifecycleComponent>() {

	private val startActions: MutableList<suspend RaptorLifecycleStartScope.() -> Unit> = mutableListOf()
	private val stopActions: MutableList<suspend RaptorLifecycleStopScope.() -> Unit> = mutableListOf()


	@RaptorDsl
	public fun onStart(action: suspend RaptorLifecycleStartScope.() -> Unit) {
		startActions += action
	}


	@RaptorDsl
	public fun onStop(action: suspend RaptorLifecycleStopScope.() -> Unit) {
		stopActions += action
	}


	override fun RaptorComponentConfigurationEndScope<RaptorLifecycleComponent>.onConfigurationEnded() {
		propertyRegistry.register(DefaultRaptorLifecycle(
			context = lazyContext,
			startActions = startActions.toList(),
			stopActions = stopActions.toList()
		))
	}


	override fun toString(): String = "lifecycle"
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStart(action: suspend RaptorLifecycleStartScope.() -> Unit) {
	this {
		onStart(action)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorLifecycleComponent>.onStop(action: suspend RaptorLifecycleStopScope.() -> Unit) {
	this {
		onStop(action)
	}
}
