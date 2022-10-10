package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*


public object RaptorDomainPlugin : RaptorPluginWithConfiguration<RaptorDomain> {

	override fun RaptorPluginCompletionScope.complete(): RaptorDomain {
		val domain = componentRegistry.one(Keys.domainComponent).complete(context = lazyContext)
		val projectionEventStream = DefaultAggregateProjectionEventStream()

		val loaderManager = DefaultAggregateProjectionLoaderManager(
			definitions = domain.aggregates.definitions.mapNotNull { it.projectionDefinition },
			projectionEventStream = projectionEventStream,
		)
		val aggregateManager = DefaultAggregateManager(
			domain = domain,
			eventFactory = domain.aggregates.eventFactory,
			fixme = loaderManager,
		)

		propertyRegistry.register(Keys.aggregateManagerProperty, aggregateManager)
		propertyRegistry.register(Keys.aggregateProjectionEventStreamProperty, projectionEventStream)
		propertyRegistry.register(Keys.aggregateProjectorLoaderManagerProperty, loaderManager)
		propertyRegistry.register(Keys.domainProperty, domain)

		return domain
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.domainComponent, RaptorDomainComponent(topLevelScope = this))

		optional(RaptorDIPlugin) {
			// FIXME di should use properties only
			di.provide<RaptorAggregateCommandExecutor> { context.aggregateManager }
			di.provide<RaptorAggregateEventStream> { DefaultAggregateEventStream() }
			di.provide<RaptorAggregateProjectionEventStream> { context[Keys.aggregateProjectionEventStreamProperty]!! } // TODO
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
public val RaptorPluginScope<in RaptorDomainPlugin>.domain: RaptorDomainComponent
	get() = componentRegistry.oneOrNull(Keys.domainComponent) ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)
