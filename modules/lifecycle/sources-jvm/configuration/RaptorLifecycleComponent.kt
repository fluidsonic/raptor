package io.fluidsonic.raptor


public class RaptorLifecycleComponent internal constructor() : RaptorComponent.Default<RaptorLifecycleComponent>() {

	internal val startActions: MutableList<suspend RaptorLifecycleStartScope.() -> Unit> = mutableListOf()
	internal val stopActions: MutableList<suspend RaptorLifecycleStopScope.() -> Unit> = mutableListOf()


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(DefaultRaptorLifecycle.PropertyKey, DefaultRaptorLifecycle(
			context = lazyContext,
			startActions = startActions.toList(),
			stopActions = stopActions.toList()
		))
	}


	override fun toString(): String = "lifecycle"


	public companion object;


	internal object Key : RaptorComponentKey<RaptorLifecycleComponent> {

		override fun toString() = "lifecycle"
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorLifecycleComponent>.onStart(action: suspend RaptorLifecycleStartScope.() -> Unit) {
	configure {
		startActions += action
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorLifecycleComponent>.onStop(action: suspend RaptorLifecycleStopScope.() -> Unit) {
	configure {
		stopActions += action
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.lifecycle: RaptorComponentSet<RaptorLifecycleComponent>
	get() = componentRegistry.configure(RaptorLifecycleComponent.Key)
