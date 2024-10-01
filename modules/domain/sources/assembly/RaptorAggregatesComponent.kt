package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
public class RaptorAggregatesComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME hack
) : RaptorComponent.Base<RaptorAggregatesComponent>(RaptorDomainPlugin),
	RaptorComponentSet<RaptorAggregateComponent<*, *, *, *>> { // FIXME ok? conflicting Set/Query esp. as we remove Set

	private val onCommittedActions: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	private var store: RaptorAggregateStore? = null


	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, *>>
		get() = componentRegistry.all(Keys.aggregateComponent).all


	internal fun completeIn(scope: RaptorPluginCompletionScope): RaptorAggregateDefinitions {
		val definitions = RaptorAggregateDefinitions(componentRegistry.many(Keys.aggregateComponent).mapTo(hashSetOf()) { it.complete() })
		val onCommittedActions = onCommittedActions.toList()
		val store = store

		scope.configure(RaptorDIPlugin) {
			di {
				provide<DefaultAggregateManager> {
					DefaultAggregateManager(
						clock = get(),
						context = get(),
						definitions = definitions,
						eventStream = get(),
						onCommittedActions = onCommittedActions,
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
				provide<RaptorAggregateProvider> { get<DefaultAggregateManager>() }
				provide<RaptorAggregateStream> { get<DefaultAggregateStream>() }

				if (store != null)
					provide<RaptorAggregateStore>(store)

				definitions
					.filter { it.isIndividual }
					.map { definition ->
						val eventType = definition.eventType
						val mangerType = RaptorIndividualAggregateManager::class.createType(
							listOf(
								KTypeProjection.invariant(definition.idType),
								KTypeProjection.invariant(definition.changeType),
							)
						)
						val storeType = RaptorIndividualAggregateStore::class.createType(
							listOf(
								KTypeProjection.invariant(definition.idType),
								KTypeProjection.invariant(definition.changeType),
							)
						)
						val storeName = "events_${definition.discriminator}"

						provide<RaptorIndividualAggregateManager<*, *>>(mangerType) {
							DefaultIndividualAggregateManager<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>(
								eventStream = get(),
								store = get(storeType),
							)
						}

						provide<RaptorIndividualAggregateStore<*, *>>(storeType) {
							get<RaptorIndividualAggregateStoreFactory>().create(name = storeName, eventType = eventType)
						}

						mangerType
					}
					.let { managerTypes ->
						provide<Collection<DefaultIndividualAggregateManager<*, *>>> {
							managerTypes.map { get<DefaultIndividualAggregateManager<*, *>>(it) }
						}
					}
			}
		}

		return definitions
	}


	@RaptorDsl
	public fun onCommitted(action: suspend RaptorScope.() -> Unit) {
		onCommittedActions += action
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
		individual: Boolean = false,
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
			individual = individual,
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
	individual: Boolean = false,
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
			individual = individual,
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
	individual: Boolean = false,
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
		individual = individual,
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
	individual: Boolean = false,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
		individual = individual,
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
	individual: Boolean = false,
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
		individual = individual,
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
	individual: Boolean = false,
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
		individual = individual,
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
	individual: Boolean = false,
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
		individual = individual,
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
	individual: Boolean = false,
): RaptorAssemblyQuery<RaptorAggregateComponent<Aggregate, Id, Command, Change>> =
	new(
		aggregateClass = Aggregate::class,
		changeClass = Change::class,
		commandClass = Command::class,
		discriminator = discriminator,
		factory = factory,
		idClass = Id::class,
		individual = individual,
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
	individual: Boolean = false,
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
		individual = individual,
	)
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.onCommitted(action: suspend RaptorScope.() -> Unit) {
	each { onCommitted(action) }
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.store(store: RaptorAggregateStore) {
	each {
		store(store)
	}
}
