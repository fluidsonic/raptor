package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.raptor.transactions.*
import kotlinx.coroutines.*


public object RaptorDomainPlugin : RaptorPluginWithConfiguration<RaptorDomainPluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorDomainPluginConfiguration {
		completeComponents()

		return componentRegistry.one(Keys.domainComponent).complete(context = lazyContext)
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.domainComponent, RaptorDomainComponent(topLevelScope = this))

		optional(RaptorDIPlugin) {
			di.provide<RaptorAggregateCommandExecutor> { context.plugins.domain.aggregates.manager }
			di.provide<RaptorAggregateEventFactory> { context.plugins.domain.aggregates.eventFactory }
			di.provide<RaptorAggregateStream> { context.plugins.domain.aggregates.eventStream }
			di.provide<RaptorAggregateProjectionEventStream> { context.plugins.domain.aggregates.projectionEventStream }
			di.provide<RaptorAggregateStore> { context.plugins.domain.aggregates.store }
		}

		require(RaptorLifecyclePlugin) {
			// FIXME Delay onStop until manager & store have completed their work.

			lifecycle {
				onStart(priority = Int.MIN_VALUE) {
					val configuration = context.plugins.domain
					configuration.aggregates.store.start()
					configuration.aggregates.manager.start()

					val actions = configuration.onLoadedActions
					configuration.onLoadedActions = emptyList()

					coroutineScope {
						actions.map { async { it() } }.awaitAll()
					}
				}
				onStop(priority = Int.MAX_VALUE) {
					val configuration = context.plugins.domain
					configuration.aggregates.manager.stop()
				}
			}
		}

		require(RaptorTransactionPlugin) {
			// FIXME
			transactions {
				observe {
					onStop {
						context.plugins.domain.aggregates.manager.commit() // FIXME per-tx
					}
				}

				onCreate {

				}
			}
		}
	}


	override fun toString(): String = "domain"
}


// FIXME lazy
@RaptorDsl
public val RaptorPluginScope<in RaptorDomainPlugin>.domain: RaptorDomainComponent
	get() = componentRegistry.oneOrNull(Keys.domainComponent) ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)
