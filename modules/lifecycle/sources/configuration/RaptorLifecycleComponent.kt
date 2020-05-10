package io.fluidsonic.raptor


class RaptorLifecycleComponent internal constructor() : RaptorComponent.Default<RaptorLifecycleComponent>() {

	internal val startActions: MutableList<suspend RaptorLifecycleStartScope.() -> Unit> = mutableListOf()
	internal val stopActions: MutableList<suspend RaptorLifecycleStopScope.() -> Unit> = mutableListOf()


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(DefaultRaptorLifecycle.PropertyKey, DefaultRaptorLifecycle(
			context = lazyContext,
			startActions = startActions.toList(),
			stopActions = stopActions.toList()
		))
	}


	override fun toString() = "lifecycle"


	companion object;


	internal object Key : RaptorComponentKey<RaptorLifecycleComponent> {

		override fun toString() = "lifecycle"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorLifecycleComponent>.onStart(action: suspend RaptorLifecycleStartScope.() -> Unit) = configure {
	startActions += action
}


@RaptorDsl
fun RaptorComponentSet<RaptorLifecycleComponent>.onStop(action: suspend RaptorLifecycleStopScope.() -> Unit) = configure {
	stopActions += action
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.lifecycle
	get() = componentRegistry.configure(RaptorLifecycleComponent.Key)
