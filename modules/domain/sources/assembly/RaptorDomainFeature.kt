package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import kotlinx.datetime.*


public object RaptorDomainFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
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


	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(Keys.domainComponent) { RaptorDomainComponent(topLevelScope = this) }

		ifFeature(RaptorDIFeature) {
			// FIXME di should use properties only
			di.provide<RaptorAggregateEventStream> { DefaultAggregateEventStream() }
			di.provide<RaptorAggregateProjectionEventStream> { DefaultAggregateProjectionEventStream() }
		}

		requireFeature(RaptorLifecycleFeature) {
			// FIXME Delay onStop until manager & store have settled.

			lifecycle.onStart {
				context.aggregateManager.load()
			}
		}

		requireFeature(RaptorTransactionFeature) {
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
public val RaptorTopLevelConfigurationScope.domain: RaptorDomainComponent
	get() = componentRegistry.oneOrNull(Keys.domainComponent) ?: throw RaptorFeatureNotInstalledException(RaptorDomainFeature)
