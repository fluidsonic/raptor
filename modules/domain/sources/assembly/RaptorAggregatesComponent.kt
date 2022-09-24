package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorAggregatesComponent internal constructor(
	private val topLevelScope: RaptorTopLevelConfigurationScope, // FIXME hack
) : RaptorComponent.Base<RaptorAggregatesComponent>(),
	RaptorComponentSet<RaptorAggregateComponent<*, *, *, *>> { // FIXME ok? conflicting Set/Query esp. as we remove Set

	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, *>>
		get() = componentRegistry.all(Keys.aggregateComponent).all


	// FIXME rework
	internal fun complete(): Set<RaptorAggregateDefinition<*, *, *, *>> =
		componentRegistry.many(Keys.aggregateComponent).mapTo(hashSetOf()) { it.complete() }


	@RaptorDsl
	public fun <
		Aggregate : RaptorAggregate<Id, Command, Event>,
		Id : RaptorAggregateId,
		Command : RaptorAggregateCommand<Id>,
		Event : RaptorAggregateEvent<Id>,
		>
		new(
		aggregateClass: KClass<Aggregate>,
		commandClass: KClass<Command>,
		discriminator: String,
		eventClass: KClass<Event>,
		factory: RaptorAggregateFactory<Aggregate, Id>,
		idClass: KClass<Id>,
	): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Event>> =
		RaptorAggregateComponent(
			aggregateClass = aggregateClass,
			commandClass = commandClass,
			discriminator = discriminator,
			eventClass = eventClass,
			factory = factory,
			idClass = idClass,
			topLevelScope = topLevelScope,
		).also { componentRegistry.register(Keys.aggregateComponent, it) }
}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	commandClass: KClass<Command>,
	discriminator: String,
	eventClass: KClass<Event>,
	factory: RaptorAggregateFactory<Aggregate, Id>,
	idClass: KClass<Id>,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Event>> =
	flatMap { aggregates ->
		aggregates.new(
			aggregateClass = aggregateClass,
			commandClass = commandClass,
			discriminator = discriminator,
			eventClass = eventClass,
			factory = factory,
			idClass = idClass,
		)
	}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	commandClass: KClass<Command>,
	discriminator: String,
	eventClass: KClass<Event>,
	factory: RaptorAggregateFactory<Aggregate, Id>,
	idClass: KClass<Id>,
	configure: RaptorAggregateComponent<Aggregate, Id, Command, Event>.() -> Unit = {},
) {
	new(
		aggregateClass = aggregateClass,
		commandClass = commandClass,
		discriminator = discriminator,
		eventClass = eventClass,
		factory = factory,
		idClass = idClass,
	).each(configure)
}


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Event>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	factory: RaptorAggregateFactory<Aggregate, Id>,
	discriminator: String,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Event>> =
	new(
		aggregateClass = Aggregate::class,
		commandClass = Command::class,
		discriminator = discriminator,
		eventClass = Event::class,
		factory = factory,
		idClass = Id::class,
	)


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Event>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Event : RaptorAggregateEvent<Id>,
	> RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	factory: RaptorAggregateFactory<Aggregate, Id>,
	discriminator: String,
	noinline configure: RaptorAggregateComponent<Aggregate, Id, Command, Event>.() -> Unit = {},
) {
	new(
		aggregateClass = Aggregate::class,
		commandClass = Command::class,
		configure = configure,
		discriminator = discriminator,
		eventClass = Event::class,
		factory = factory,
		idClass = Id::class,
	)
}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	commandClass: KClass<Command>,
	discriminator: String,
	eventClass: KClass<Event>,
	factory: (id: Id) -> Aggregate,
	idClass: KClass<Id>,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Event>> =
	new(
		aggregateClass = aggregateClass,
		commandClass = commandClass,
		discriminator = discriminator,
		eventClass = eventClass,
		factory = RaptorAggregateFactory(factory),
		idClass = idClass,
	)


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Event>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	commandClass: KClass<Command>,
	discriminator: String,
	eventClass: KClass<Event>,
	factory: (id: Id) -> Aggregate,
	idClass: KClass<Id>,
	configure: RaptorAggregateComponent<Aggregate, Id, Command, Event>.() -> Unit = {},
) {
	new(
		aggregateClass = aggregateClass,
		commandClass = commandClass,
		discriminator = discriminator,
		eventClass = eventClass,
		factory = RaptorAggregateFactory(factory),
		idClass = idClass,
	).each(configure)
}


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Event>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Event : RaptorAggregateEvent<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	noinline factory: (id: Id) -> Aggregate,
	discriminator: String,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Event>> =
	new(
		aggregateClass = Aggregate::class,
		commandClass = Command::class,
		discriminator = discriminator,
		eventClass = Event::class,
		factory = factory,
		idClass = Id::class,
	)


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Event>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Event : RaptorAggregateEvent<Id>,
	> RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	noinline factory: (id: Id) -> Aggregate,
	discriminator: String,
	noinline configure: RaptorAggregateComponent<Aggregate, Id, Command, Event>.() -> Unit = {},
) {
	new(
		aggregateClass = Aggregate::class,
		commandClass = Command::class,
		configure = configure,
		discriminator = discriminator,
		eventClass = Event::class,
		factory = factory,
		idClass = Id::class,
	)
}
