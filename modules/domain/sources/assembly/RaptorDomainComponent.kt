package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.time.*
import kotlinx.coroutines.*
import org.slf4j.*


@RaptorDsl
public class RaptorDomainComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME remove hack
) : RaptorComponent.Base<RaptorDomainComponent>(RaptorDomainPlugin) {

	private val onLoadedActions: MutableList<LoadedAction> = mutableListOf()


	@RaptorDsl
	public val aggregates: RaptorAggregatesComponent
		get() = componentRegistry.one(Keys.aggregatesComponent)


	// TODO rework & standardize
	@OptIn(ExperimentalTime::class)
	internal fun completeIn(scope: RaptorPluginCompletionScope): RaptorAggregateDefinitions {
		var onLoadedActions = onLoadedActions.toList()

		val definitions = aggregates.completeIn(scope)

		scope.configure(RaptorLifecyclePlugin) {
			lifecycle {
				// +3 to start before services and allow for starts between services and domain.
				onStart("domain", priority = Int.MIN_VALUE + 3) {
					val logger: Logger = context.di.get()

					val storeStartDuration = measureTime {
						context.aggregateStore.start()
					}
					logger.debug("Started 'domain: aggregate store' in $storeStartDuration.")

					val managerStartDuration = measureTime {
						context.aggregateManager.start(individualManagers = context.di.get())
					}
					logger.debug("Started 'domain: aggregate manager' in $managerStartDuration.")

					val actions = onLoadedActions
					onLoadedActions = emptyList()

					val loadCallbackDuration = measureTime {
						coroutineScope {
							actions.map { action ->
								async {
									val duration = measureTime { action.block(this@onStart) }
									logger.debug("Started 'domain: load callback: ${action.label}' in $duration.")
								}
							}.awaitAll()
						}
					}
					logger.debug("Started 'domain: load callbacks' in $loadCallbackDuration.")
				}

				// FIXME Delay onStop until manager & store have completed their work.
				onStop("domain", priority = Int.MIN_VALUE) {
					context.aggregateManager.stop()
				}
			}
		}

		return definitions
	}


	@RaptorDsl
	public fun onLoaded(label: String, block: suspend RaptorScope.() -> Unit) {
		onLoadedActions += LoadedAction(block = block, label = label)
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(Keys.aggregatesComponent, RaptorAggregatesComponent(topLevelScope = topLevelScope))
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorDomainComponent>.aggregates: RaptorAssemblyQuery<RaptorAggregatesComponent>
	get() = map { it.aggregates }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDomainComponent>.onLoaded(
	label: String,
	block: suspend RaptorScope.() -> Unit,
) {
	each { onLoaded(label, block) }
}


private class LoadedAction(
	val block: suspend RaptorScope.() -> Unit,
	val label: String,
)
