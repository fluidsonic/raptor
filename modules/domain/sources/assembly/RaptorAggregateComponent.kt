package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.collections.set
import kotlin.reflect.*
import kotlin.reflect.full.*


public class RaptorAggregateComponent<
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	> internal constructor(
	private val aggregateClass: KClass<Aggregate>, // FIXME use KTypes
	private val commandClass: KClass<Command>,
	private val discriminator: String,
	private val eventClass: KClass<Event>,
	private val factory: RaptorAggregateFactory<Aggregate, Id>,
	private val idClass: KClass<Id>,
	private val topLevelScope: RaptorTopLevelConfigurationScope,
) : RaptorComponent.Base<RaptorAggregateComponent<Aggregate, Id, Command, Event>>() {

	private val commandDefinitions: MutableMap<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> = hashMapOf()
	private val eventDefinitions: MutableMap<KClass<out Event>, RaptorAggregateEventDefinition<Id, out Event>> = hashMapOf()
	private var projectorFactory: (() -> RaptorProjector.Incremental<*, Id, Event>)? = null


	@RaptorDsl
	public fun command(commandClass: KClass<out Command>) {
		check(!commandDefinitions.containsKey(commandClass)) { "Cannot define command $commandClass multiple times." }

		commandDefinitions[commandClass] = RaptorAggregateCommandDefinition(commandClass = commandClass)
	}


	internal fun complete() =
		RaptorAggregateDefinition(
			aggregateClass = aggregateClass,
			commandClass = commandClass,
			commandDefinitions = commandDefinitions.values.toHashSet(),
			discriminator = discriminator,
			eventClass = eventClass,
			eventDefinitions = eventDefinitions.values.toHashSet(),
			factory = factory,
			idClass = idClass,
		)


	@RaptorDsl
	public fun event(discriminator: String, eventClass: KClass<out Event>) {
		check(!eventDefinitions.containsKey(eventClass)) { "Cannot define event $eventClass multiple times." }
		check(eventDefinitions.values.none { it.discriminator == discriminator }) {
			"Cannot define multiple events with the same discriminator: $discriminator."
		}

		eventDefinitions[eventClass] = RaptorAggregateEventDefinition(
			discriminator = discriminator,
			eventClass = eventClass,
		)
	}


	@RaptorInternalApi // FIXME
	@RaptorDsl
	public fun project(type: KType, projectorFactory: () -> RaptorProjector.Incremental<*, Id, Event>) {
		check(this.projectorFactory == null) { "Cannot set multiple projector factories for aggregate $aggregateClass." }

		this.projectorFactory = projectorFactory

		with(topLevelScope) { // FIXME remove hack
			ifFeature(RaptorDIFeature) {
				di.provide(RaptorProjectionLoader::class.createType(listOf(
					KTypeProjection.invariant(type),
					KTypeProjection.invariant(this@RaptorAggregateComponent.idClass.starProjectedType),
				))) { DefaultProjectionLoader<RaptorProjection<*>, RaptorProjectionId>() }
			}
		}
	}
}


@RaptorDsl
public inline fun <reified Command : RaptorAggregateCommand<*>> RaptorAssemblyQuery<RaptorAggregateComponent<*, *, in Command, *>>.command() {
	each {
		command(Command::class)
	}
}


@RaptorDsl
public fun <Event : RaptorAggregateEvent<*>>
	RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, in Event>>.event(
	discriminator: String,
	eventClass: KClass<Event>,
) {
	each {
		event(discriminator, eventClass)
	}
}


@RaptorDsl
public inline fun <reified Event : RaptorAggregateEvent<*>>
	RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, in Event>>.event(
	discriminator: String,
) {
	event(discriminator, Event::class)
}


@OptIn(RaptorInternalApi::class) // FIXME remove
@RaptorDsl
public fun <Projection : RaptorProjection<Id>, Id, Event : RaptorAggregateEvent<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	type: KType,
	projectorFactory: () -> RaptorProjector.Incremental<Projection, Id, Event>,
) where Id : RaptorAggregateId, Id : RaptorProjectionId {
	each {
		project(type, projectorFactory)
	}
}


@RaptorDsl
public inline fun <reified Projection : RaptorProjection<Id>, Id, Event : RaptorAggregateEvent<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	noinline projectorFactory: () -> RaptorProjector.Incremental<Projection, Id, Event>,
) where Id : RaptorAggregateId, Id : RaptorProjectionId {
	project<Projection, Id, Event>(typeOf<Projection>(), projectorFactory)
}
