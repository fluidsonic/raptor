package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


internal object Keys {

	val aggregateManagerProperty = RaptorPropertyKey<RaptorAggregateManager>("aggregate manager")
	val aggregateComponent = RaptorComponentKey<RaptorAggregateComponent<*, *, *, *>>("aggregate")
	val aggregatesComponent = RaptorComponentKey<RaptorAggregatesComponent>("aggregates")
	val domainComponent = RaptorComponentKey<RaptorDomainComponent>("domain")
	val domainProperty = RaptorPropertyKey<RaptorDomain>("domain")
}
