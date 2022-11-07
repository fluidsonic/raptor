package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.lifecycle.*
import kotlinx.coroutines.*


@RaptorDsl
public class RaptorDomainComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME remove hack
) : RaptorComponent.Base<RaptorDomainComponent>(RaptorDomainPlugin) {

	private val onLoadedActions: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	@RaptorDsl
	public val aggregates: RaptorAggregatesComponent
		get() = componentRegistry.one(Keys.aggregatesComponent)


	// TODO rework & standardize
	internal fun completeIn(scope: RaptorPluginCompletionScope): RaptorAggregateDefinitions {
		var onLoadedActions = onLoadedActions.toList()

		val definitions = aggregates.completeIn(scope)

		scope.configure(RaptorLifecyclePlugin) {
			lifecycle {
				onStart(priority = Int.MIN_VALUE) {
					context.aggregateStore.start()
					context.aggregateManager.start()

					val actions = onLoadedActions
					onLoadedActions = emptyList()

					coroutineScope {
						actions.map { async { it() } }.awaitAll()
					}
				}

				// FIXME Delay onStop until manager & store have completed their work.
				onStop(priority = Int.MIN_VALUE) {
					context.aggregateManager.stop()
				}
			}
		}

		return definitions
	}


	@RaptorDsl
	public fun onLoaded(action: suspend RaptorScope.() -> Unit) {
		onLoadedActions += action
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(Keys.aggregatesComponent, RaptorAggregatesComponent(topLevelScope = topLevelScope))
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorDomainComponent>.aggregates: RaptorAssemblyQuery<RaptorAggregatesComponent>
	get() = map { it.aggregates }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDomainComponent>.onLoaded(action: suspend RaptorScope.() -> Unit) {
	each { onLoaded(action) }
}
