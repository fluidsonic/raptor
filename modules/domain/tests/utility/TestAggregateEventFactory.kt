import io.fluidsonic.raptor.cqrs.*
import kotlinx.datetime.*


class TestAggregateEventFactory(
	private val clock: Clock,
) : RaptorAggregateEventFactory {

	private var nextId = 1


	override fun <Id : RaptorAggregateId, Event : RaptorAggregateChange<Id>> create(
		aggregateId: Id,
		data: Event,
		version: Int,
	): RaptorAggregateEvent<Id, Event> =
		RaptorAggregateEvent(
			aggregateId = aggregateId,
			data = data,
			id = RaptorAggregateEventId((nextId++).toString()),
			timestamp = clock.now(),
			version = version,
		)
}
