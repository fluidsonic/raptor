package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.cqrs.*
import kotlinx.datetime.*
import org.bson.types.*


public class RaptorMongoAggregateEventFactory(
	private val clock: Clock,
) : RaptorAggregateEventFactory {

	override fun <Id : RaptorAggregateId, Event : RaptorAggregateChange<Id>> create(
		aggregateId: Id,
		change: Event,
		version: Int,
	): RaptorAggregateEvent<Id, Event> =
		RaptorAggregateEvent(
			aggregateId = aggregateId,
			change = change,
			id = RaptorAggregateEventId(ObjectId.get().toString()),
			timestamp = clock.now(),
			version = version,
		)
}
