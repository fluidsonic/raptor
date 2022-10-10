package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


internal object Keys {

	val aggregateComponent = RaptorComponentKey<RaptorAggregateComponent<*, *, *, *>>("aggregate")
	val aggregatesComponent = RaptorComponentKey<RaptorAggregatesComponent>("aggregates")
	val domainComponent = RaptorComponentKey<RaptorDomainComponent>("domain")
}
