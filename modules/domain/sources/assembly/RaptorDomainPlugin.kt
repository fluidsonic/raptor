package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import kotlinx.datetime.*


public object RaptorDomainPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		val domain = componentRegistry.one(Keys.domainComponent).complete()

		propertyRegistry.register(Keys.aggregateManagerProperty, DefaultAggregateManager(
			domain = domain,
			eventFactory = DefaultEventFactory(
				clock = Clock.System, // FIXME
				idFactory = { RaptorEventId("x") }, // FIXME
			),
		))
		propertyRegistry.register(Keys.domainProperty, domain)
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.domainComponent) { RaptorDomainComponent(topLevelScope = this) }

		optional(RaptorDIPlugin) {
			// FIXME di should use properties only
			di.provide<RaptorAggregateEventStream> { DefaultAggregateEventStream() }
			di.provide<RaptorAggregateProjectionEventStream> { DefaultAggregateProjectionEventStream() }
		}

		require(RaptorLifecyclePlugin) {
			// FIXME Delay onStop until manager & store have settled.

			lifecycle.onStart {
				context.aggregateManager.load()
			}
		}

		require(RaptorTransactionPlugin) {
			// FIXME
			transactions {
				observe {
					onStop {
						context.aggregateManager.commit() // FIXME per-tx
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
public val RaptorAssemblyScope.domain: RaptorDomainComponent
	get() = componentRegistry.oneOrNull(Keys.domainComponent) ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)
