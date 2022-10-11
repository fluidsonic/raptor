import io.fluidsonic.raptor.domain.*
import kotlinx.datetime.*


class TestAggregateEventFactory(
	private val clock: Clock,
) : RaptorAggregateEventFactory {

	private var nextId = 1


	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> create(
		aggregateId: Id,
		change: Change,
		version: Int,
	): RaptorAggregateEvent<Id, Change> =
		RaptorAggregateEvent(
			aggregateId = aggregateId,
			change = change,
			id = RaptorAggregateEventId((nextId++).toString()),
			isReplay = false,
			timestamp = clock.now(),
			version = version,
		)
}
