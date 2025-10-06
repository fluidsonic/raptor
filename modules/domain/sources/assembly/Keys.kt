package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


internal object Keys {

	private val _aggregateComponent = RaptorComponentKey<RaptorAggregateComponent<
		RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>,
		RaptorAggregateId,
		RaptorAggregateCommand<RaptorAggregateId>,
		RaptorAggregateChange<RaptorAggregateId>,
		>>("aggregate")

	fun aggregateComponent(): RaptorComponentKey<RaptorAggregateComponent<
		RaptorAggregate<RaptorAggregateId, RaptorAggregateCommand<RaptorAggregateId>, RaptorAggregateChange<RaptorAggregateId>>,
		RaptorAggregateId,
		RaptorAggregateCommand<RaptorAggregateId>,
		RaptorAggregateChange<RaptorAggregateId>,
		>> =
		_aggregateComponent

	fun <Component : RaptorAggregateComponent<*, *, *, *>> aggregateComponentOf() =
		_aggregateComponent as RaptorComponentKey<Component>

	val aggregatesComponent = RaptorComponentKey<RaptorAggregatesComponent>("aggregates")
	val domainComponent = RaptorComponentKey<RaptorDomainComponent>("domain")
}
