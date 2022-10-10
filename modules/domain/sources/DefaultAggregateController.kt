package io.fluidsonic.raptor.domain


internal class DefaultAggregateController<
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateChange<Id>,
	>(
	private val definition: RaptorAggregateDefinition<Aggregate, Id, Command, Event>,
	private val eventFactory: RaptorAggregateEventFactory,
	private val id: Id,
) : RaptorAggregateController<Id> {

	private val aggregate: Aggregate = definition.factory.create(id)
	private var version = 0


	override fun execute(command: RaptorAggregateCommand<Id>): List<RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>> {
		@Suppress("NAME_SHADOWING")
		val command = definition.castOrNull(command)
			?: error("Unsupported command.") // FIXME

		return aggregate.execute(command).map { change ->
			eventFactory.create(
				aggregateId = aggregate.id,
				change = change,
				version = version + 1,
			).also { event ->
				handleUnchecked(event)
			}
		}
	}


	override fun handle(event: RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>) {
		@Suppress("NAME_SHADOWING")
		val event = definition.castOrNull(event)
			?: error("Unsupported event.") // FIXME

		check(event.aggregateId == id) { "Cannot apply an event for a aggregate '${event.aggregateId.debug}' to aggregate '${id.debug}':\n$event" }
		check(event.version == version + 1) {
			when {
				event.version <= version -> "Cannot apply multiple events with the same version to aggregate '${id.debug}':\n$event"
				else -> "Cannot apply events out of order (expected version ${version + 1}, " +
					"got ${event.version}) to aggregate '${id.debug}': $event"
			}
		}

		handleUnchecked(event)
	}


	private fun handleUnchecked(event: RaptorAggregateEvent<Id, Event>) {
		aggregate.handle(event.change)
		version = event.version
	}
}
