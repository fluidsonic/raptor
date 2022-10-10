package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlin.reflect.full.*


public data class RaptorAggregateDefinition<
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	>(
	val aggregateClass: KClass<Aggregate>,
	val changeClass: KClass<Change>,
	val changeDefinitions: Set<RaptorAggregateChangeDefinition<Id, out Change>>,
	val commandClass: KClass<Command>,
	val commandDefinitions: Set<RaptorAggregateCommandDefinition<Id, out Command>>,
	val discriminator: String,
	val factory: RaptorAggregateFactory<Aggregate, Id>,
	val idClass: KClass<Id>,
	val projectionDefinition: RaptorAggregateProjectionDefinition<*, *, Change>? = null,
) {

	private val changeDefinitionsByClass: Map<KClass<out Change>, RaptorAggregateChangeDefinition<Id, out Change>> =
		changeDefinitions.associateByTo(hashMapOf()) { it.changeClass }

	private val changeDefinitionsByDiscriminator: Map<String, RaptorAggregateChangeDefinition<Id, out Change>> =
		changeDefinitions.associateByTo(hashMapOf()) { it.discriminator }

	private val commandDefinitionsByClass: Map<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> =
		commandDefinitions.associateByTo(hashMapOf()) { it.commandClass }

	val idType: KType = idClass.starProjectedType


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(change: RaptorAggregateChange<Id>): Change? {
		if (changeDefinition(change::class) == null)
			return null

		return change as Change
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(command: RaptorAggregateCommand<Id>): Command? {
		if (commandDefinition(command::class) == null)
			return null

		return command as Command
	}


	@Suppress("UNCHECKED_CAST")
	public fun castOrNull(event: RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>): RaptorAggregateEvent<Id, Change>? {
		if (castOrNull(event.change) == null)
			return null

		return event as RaptorAggregateEvent<Id, Change>
	}


	@Suppress("UNCHECKED_CAST")
	public fun <TEvent : RaptorAggregateChange<Id>> changeDefinition(
		changeClass: KClass<out TEvent>,
	): RaptorAggregateChangeDefinition<Id, out Change>? =
		changeDefinitionsByClass[changeClass as KClass<out Change>]


	public fun changeDefinition(discriminator: String): RaptorAggregateChangeDefinition<Id, out Change>? =
		changeDefinitionsByDiscriminator[discriminator]


	@Suppress("UNCHECKED_CAST")
	public fun <TCommand : RaptorAggregateCommand<Id>> commandDefinition(
		commandClass: KClass<out TCommand>,
	): RaptorAggregateCommandDefinition<Id, out Command>? =
		commandDefinitionsByClass[commandClass as KClass<out Command>]
}
