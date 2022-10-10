package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


internal object Keys {

	val aggregateManagerProperty = RaptorPropertyKey<DefaultAggregateManager>("aggregate manager")
	val aggregateComponent = RaptorComponentKey<RaptorAggregateComponent<*, *, *, *>>("aggregate")
	val aggregateProjectionEventStreamProperty = RaptorPropertyKey<RaptorAggregateProjectionEventStream>("aggregate projection event stream")
	val aggregateProjectorLoaderManagerProperty = RaptorPropertyKey<RaptorAggregateProjectorLoaderManager>("aggregate projector loader manager")
	val aggregatesComponent = RaptorComponentKey<RaptorAggregatesComponent>("aggregates")
	val domainComponent = RaptorComponentKey<RaptorDomainComponent>("domain")
	val domainProperty = RaptorPropertyKey<RaptorDomain>("domain")
}
