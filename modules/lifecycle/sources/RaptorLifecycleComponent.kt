package io.fluidsonic.raptor


@RaptorDsl
class RaptorLifecycleComponent internal constructor() : RaptorComponent.Base<RaptorLifecycleComponent>() {

	internal val onStartActions: MutableList<suspend RaptorLifecycleStartScope.() -> Unit> = mutableListOf()
	internal val onStopActions: MutableList<suspend RaptorLifecycleStopScope.() -> Unit> = mutableListOf()


	internal fun finalize() =
		DefaultRaptorLifecycle(
			onStartActions = onStartActions.toList(),
			onStopActions = onStopActions.toList()
		)


	override fun toString() = "lifecycle"


	companion object;


	internal object Key : RaptorComponentKey<RaptorLifecycleComponent> {

		override fun toString() = "lifecycle"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorLifecycleComponent>.onStart(action: suspend RaptorLifecycleStartScope.() -> Unit) = configure {
	onStartActions += action
}


@RaptorDsl
fun RaptorComponentSet<RaptorLifecycleComponent>.onStop(action: suspend RaptorLifecycleStopScope.() -> Unit) = configure {
	onStopActions += action
}


@RaptorDsl
val RaptorGlobalConfigurationScope.lifecycle
	get() = componentRegistry.configure(RaptorLifecycleComponent.Key)
