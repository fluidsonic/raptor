package io.fluidsonic.raptor.cqrs

import kotlinx.datetime.*


internal class DefaultEventFactory(
	private val clock: Clock,
	private val idFactory: RaptorEntityIdFactory<RaptorEventId>, // FIXME move this to Store?
) : RaptorEventFactory {

	// FIXME move this to Store?
	override fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorEvent<Id, Event> =
		RaptorEvent(
			aggregateId = aggregateId,
			data = data,
			id = idFactory.create(),
			timestamp = clock.now(),
			version = version,
		)
}
