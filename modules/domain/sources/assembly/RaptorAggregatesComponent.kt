package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import kotlin.reflect.*
import kotlinx.coroutines.flow.*


@RaptorDsl
public class RaptorAggregatesComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME hack
) : RaptorComponent.Base<RaptorAggregatesComponent>(RaptorDomainPlugin),
	RaptorComponentSet<RaptorAggregateComponent<*, *, *, *>> { // FIXME ok? conflicting Set/Query esp. as we remove Set

	private var eventFactory: RaptorAggregateEventFactory? = null
	private var store: RaptorAggregateStore? = null


	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, *>>
		get() = componentRegistry.all(Keys.aggregateComponent).all


	// FIXME rework
	internal fun complete(context: RaptorContext) = RaptorDomain.Aggregates(
		definitions = componentRegistry.many(Keys.aggregateComponent).mapTo(hashSetOf()) { it.complete() },
		eventFactory = when (val eventFactory = eventFactory) {
			null -> error("An aggregate event factory must be defined: domain.aggregates.eventFactory(…)")
			DIPlaceholder -> DIAggregateEventFactory(context = context)
			else -> eventFactory
		},
		store = when (val store = store) {
			null -> error("An aggregate store must be defined: domain.aggregates.store(…)")
			DIPlaceholder -> DIAggregateStore(context = context)
			else -> store
		},
	)


	@RaptorDsl
	public fun eventFactory(factory: RaptorAggregateEventFactory) {
		check(this.eventFactory == null) { "Cannot set multiple aggregate event factories." }

		this.eventFactory = factory
	}


	@RaptorDsl
	public fun <
		Aggregate : RaptorAggregate<Id, Command, Change>,
		Id : RaptorAggregateId,
		Command : RaptorAggregateCommand<Id>,
		Change : RaptorAggregateChange<Id>,
		>
		new(
		aggregateClass: KClass<Aggregate>,
		changeClass: KClass<Change>,
		commandClass: KClass<Command>,
		discriminator: String,
		factory: RaptorAggregateFactory<Aggregate, Id>,
		idClass: KClass<Id>,
	): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
		RaptorAggregateComponent(
			aggregateClass = aggregateClass,
			changeClass = changeClass,
			commandClass = commandClass,
			discriminator = discriminator,
			factory = factory,
			idClass = idClass,
			topLevelScope = topLevelScope,
		).also { componentRegistry.register(Keys.aggregateComponent, it) }


	@RaptorDsl
	public fun store(store: RaptorAggregateStore) {
		check(this.store == null) { "Cannot set multiple aggregate stores." }

		this.store = store
	}


	// FIXME hack
	internal object DIPlaceholder : RaptorAggregateEventFactory, RaptorAggregateStore {

		override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
			error("Placeholder.")
		}

		override fun load(): Flow<RaptorAggregateEvent<*, *>> {
			error("Placeholder.")
		}

		override fun <Id : RaptorAggregateId, Event : RaptorAggregateChange<Id>> create(aggregateId: Id, data: Event, version: Int): RaptorAggregateEvent<Id, Event> {
			error("Placeholder.")
		}
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.diEventFactory() {
	eventFactory(RaptorAggregatesComponent.DIPlaceholder)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.diStore() {
	store(RaptorAggregatesComponent.DIPlaceholder)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.eventFactory(factory: RaptorAggregateEventFactory) {
	each {
		eventFactory(factory)
	}
}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	changeClass: KClass<Change>,
	commandClass: KClass<Command>,
	discriminator: String,
	factory: RaptorAggregateFactory<Aggregate, Id>,
	idClass: KClass<Id>,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	flatMap { aggregates ->
		aggregates.new(
			aggregateClass = aggregateClass,
			changeClass = changeClass,
			commandClass = commandClass,
			discriminator = discriminator,
			factory = factory,
			idClass = idClass,
		)
	}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	changeClass: KClass<Change>,
	commandClass: KClass<Command>,
	discriminator: String,
	factory: RaptorAggregateFactory<Aggregate, Id>,
	idClass: KClass<Id>,
	configure: RaptorAggregateComponent<Aggregate, Id, Command, Change>.() -> Unit = {},
) {
	new(
		aggregateClass = aggregateClass,
		changeClass = changeClass,
		commandClass = commandClass,
		discriminator = discriminator,
		factory = factory,
		idClass = idClass,
	).each(configure)
}


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Change>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	factory: RaptorAggregateFactory<Aggregate, Id>,
	discriminator: String,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
	)


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Change>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Change : RaptorAggregateChange<Id>,
	> RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	factory: RaptorAggregateFactory<Aggregate, Id>,
	discriminator: String,
	noinline configure: RaptorAggregateComponent<Aggregate, Id, Command, Change>.() -> Unit = {},
) {
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		configure = configure,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
	)
}


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	changeClass: KClass<Change>,
	commandClass: KClass<Command>,
	discriminator: String,
	factory: (id: Id) -> Aggregate,
	idClass: KClass<Id>,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	new(
		aggregateClass = aggregateClass,
		changeClass = changeClass,
		commandClass = commandClass,
		discriminator = discriminator,
		factory = RaptorAggregateFactory(factory),
		idClass = idClass,
	)


@RaptorDsl
public fun <
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	aggregateClass: KClass<Aggregate>,
	changeClass: KClass<Change>,
	commandClass: KClass<Command>,
	discriminator: String,
	factory: (id: Id) -> Aggregate,
	idClass: KClass<Id>,
	configure: RaptorAggregateComponent<Aggregate, Id, Command, Change>.() -> Unit = {},
) {
	new(
		aggregateClass = aggregateClass,
		changeClass = changeClass,
		commandClass = commandClass,
		discriminator = discriminator,
		factory = RaptorAggregateFactory(factory),
		idClass = idClass,
	).each(configure)
}


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Change>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Change : RaptorAggregateChange<Id>,
	>
	RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	noinline factory: (id: Id) -> Aggregate,
	discriminator: String,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
	)


@RaptorDsl
public inline fun <
	reified Aggregate : RaptorAggregate<Id, Command, Change>,
	reified Id : RaptorAggregateId,
	reified Command : RaptorAggregateCommand<Id>,
	reified Change : RaptorAggregateChange<Id>,
	> RaptorAssemblyQuery<RaptorAggregatesComponent>.new(
	noinline factory: (id: Id) -> Aggregate,
	discriminator: String,
	noinline configure: RaptorAggregateComponent<Aggregate, Id, Command, Change>.() -> Unit = {},
) {
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		configure = configure,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
	)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.store(store: RaptorAggregateStore) {
	each {
		store(store)
	}
}
