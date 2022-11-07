package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorAggregatesComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME hack
) : RaptorComponent.Base<RaptorAggregatesComponent>(RaptorDomainPlugin),
	RaptorComponentSet<RaptorAggregateComponent<*, *, *, *>> { // FIXME ok? conflicting Set/Query esp. as we remove Set

	private var store: RaptorAggregateStore? = null


	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, *>>
		get() = componentRegistry.all(Keys.aggregateComponent).all


	internal fun completeIn(scope: RaptorPluginCompletionScope): RaptorAggregateDefinitions {
		val definitions = RaptorAggregateDefinitions(componentRegistry.many(Keys.aggregateComponent).mapTo(hashSetOf()) { it.complete() })
		val store = store

		scope.configure(RaptorDIPlugin) {
			di {
				provide<DefaultAggregateManager> {
					DefaultAggregateManager(
						clock = get(),
						definitions = definitions,
						eventStream = get(),
						projectionEventStream = get(),
						projectionLoaderManager = get(),
						store = get(),
					)
				}
				provide<DefaultAggregateProjectionLoaderManager> {
					DefaultAggregateProjectionLoaderManager(
						definitions = get<RaptorAggregateDefinitions>().mapNotNull { it.projectionDefinition },
					)
				}
				provide<DefaultAggregateProjectionStream> {
					DefaultAggregateProjectionStream()
				}
				provide<DefaultAggregateStream> {
					DefaultAggregateStream()
				}

				provide<RaptorAggregateCommandExecutor> { get<DefaultAggregateManager>() }
				provide<RaptorAggregateDefinitions>(definitions)
				provide<RaptorAggregateProjectionLoaderManager> { get<DefaultAggregateProjectionLoaderManager>() }
				provide<RaptorAggregateProjectionStream> { get<DefaultAggregateProjectionStream>() }
				provide<RaptorAggregateStream> { get<DefaultAggregateStream>() }

				if (store != null)
					provide<RaptorAggregateStore>(store)
			}
		}

		return definitions
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
