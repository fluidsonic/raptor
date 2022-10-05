package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.cqrs.*
import kotlinx.datetime.*
import org.bson.types.*


public class RaptorMongoAggregateEventFactory(
	private val clock: Clock,
) : RaptorAggregateEventFactory {

	override fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorEvent<Id, Event> =
		RaptorEvent(
			aggregateId = aggregateId,
			data = data,
			id = RaptorEventId(ObjectId.get().toString()),
			timestamp = clock.now(),
			version = version,
		)
}
