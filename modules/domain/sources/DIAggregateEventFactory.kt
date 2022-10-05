package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


// FIXME improve
internal class DIAggregateEventFactory(
	private val context: RaptorContext,
) : RaptorAggregateEventFactory {

	private val delegate: RaptorAggregateEventFactory by lazy { context.di.get() }


	override fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(aggregateId: Id, data: Event, version: Int) =
		delegate.create(aggregateId = aggregateId, data = data, version = version)
}
