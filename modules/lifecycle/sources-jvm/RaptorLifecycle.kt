package io.fluidsonic.raptor

import kotlinx.coroutines.*


private val propertyKey = RaptorPropertyKey<RaptorLifecycle>("lifecycle")


public interface RaptorLifecycle : CoroutineScope {

	public val state: State

	public suspend fun startIn(scope: CoroutineScope)
	public suspend fun stop()


	public enum class State {

		started,
		starting,
		stopped,
		stopping
	}
}


public val Raptor.lifecycle: RaptorLifecycle
	get() = context.lifecycle


internal val RaptorContext.lifecycle: RaptorLifecycle
	get() = properties[propertyKey] ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)


internal fun RaptorPropertyRegistry.register(lifecycle: RaptorLifecycle) {
	register(propertyKey, lifecycle)
}
