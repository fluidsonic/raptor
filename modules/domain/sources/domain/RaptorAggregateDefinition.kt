package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlin.reflect.full.*


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

	private val commandDefinitionsByClass: Map<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> =
		commandDefinitions.associateByTo(hashMapOf()) { it.commandClass }

	private val eventDefinitionsByClass: Map<KClass<out Event>, RaptorAggregateEventDefinition<Id, out Event>> =
		eventDefinitions.associateByTo(hashMapOf()) { it.eventClass }

	private val eventDefinitionsByDiscriminator: Map<String, RaptorAggregateEventDefinition<Id, out Event>> =
		eventDefinitions.associateByTo(hashMapOf()) { it.discriminator }

	val idType: KType = idClass.starProjectedType


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(command: RaptorAggregateCommand<Id>): Command? {
		if (commandDefinition(command::class) == null)
			return null

		return command as Command
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(event: RaptorAggregateEvent<Id>): Event? {
		if (changeDefinition(event::class) == null)
			return null

		return event as Event
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(event: RaptorEvent<Id, RaptorAggregateEvent<Id>>): RaptorEvent<Id, Event>? {
		if (castOrNull(event.data) == null)
			return null

		return event as RaptorEvent<Id, Event>
	}


	@Suppress("UNCHECKED_CAST")
	public fun <TEvent : RaptorAggregateEvent<Id>> changeDefinition(
		changeClass: KClass<out TEvent>,
	): RaptorAggregateEventDefinition<Id, out Event>? =
		eventDefinitionsByClass[changeClass as KClass<out Event>]


	public fun changeDefinition(discriminator: String): RaptorAggregateEventDefinition<Id, out Event>? =
		eventDefinitionsByDiscriminator[discriminator]


	@Suppress("UNCHECKED_CAST")
	public fun <TCommand : RaptorAggregateCommand<Id>> commandDefinition(
		commandClass: KClass<out TCommand>,
	): RaptorAggregateCommandDefinition<Id, out Command>? =
		commandDefinitionsByClass[commandClass as KClass<out Command>]
}
