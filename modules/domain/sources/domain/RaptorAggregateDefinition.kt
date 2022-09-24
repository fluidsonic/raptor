package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorAggregateDefinition<
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	>(
	val aggregateClass: KClass<Aggregate>,
	val commandClass: KClass<Command>,
	val commandDefinitions: Set<RaptorAggregateCommandDefinition<Id, out Command>>,
	val discriminator: String,
	val eventClass: KClass<Event>,
	val eventDefinitions: Set<RaptorAggregateEventDefinition<Id, out Event>>,
	val factory: RaptorAggregateFactory<Aggregate, Id>,
	val idClass: KClass<Id>,
) {

	private val commandDefinitionsByCommandClass: Map<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> =
		commandDefinitions.associateByTo(hashMapOf()) { it.commandClass }


	private val eventDefinitionsByEventClass: Map<KClass<out Event>, RaptorAggregateEventDefinition<Id, out Event>> =
		eventDefinitions.associateByTo(hashMapOf()) { it.eventClass }


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(command: RaptorAggregateCommand<Id>): Command? {
		if (!commandDefinitionsByCommandClass.contains(command::class as KClass<out Command>))
			return null

		return command as Command
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(event: RaptorAggregateEvent<Id>): Event? {
		if (!eventDefinitionsByEventClass.contains(event::class as KClass<out Event>))
			return null

		return event as Event
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(event: RaptorEvent<Id, RaptorAggregateEvent<Id>>): RaptorEvent<Id, Event>? {
		if (castOrNull(event.data) == null)
			return null

		return event as RaptorEvent<Id, Event>
	}
}
