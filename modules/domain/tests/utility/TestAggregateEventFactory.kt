import io.fluidsonic.raptor.cqrs.*
import kotlinx.datetime.*


class TestAggregateEventFactory(
	private val clock: Clock,
) : RaptorAggregateEventFactory {

	private var nextId = 1


	override fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorEvent<Id, Event> =
		RaptorEvent(
			aggregateId = aggregateId,
			data = data,
			id = RaptorEventId((nextId++).toString()),
			timestamp = clock.now(),
			version = version,
		)
}
