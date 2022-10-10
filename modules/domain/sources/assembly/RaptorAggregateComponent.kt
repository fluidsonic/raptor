package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import kotlin.collections.set
import kotlin.reflect.*
import kotlin.reflect.full.*


public class RaptorAggregateComponent<
	Aggregate : RaptorAggregate<Id, Command, Change>,
	Id : RaptorAggregateId,
	Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	> internal constructor(
	private val aggregateClass: KClass<Aggregate>, // FIXME use KTypes
	private val changeClass: KClass<Change>,
	private val commandClass: KClass<Command>,
	private val discriminator: String,
	private val factory: RaptorAggregateFactory<Aggregate, Id>,
	private val idClass: KClass<Id>,
	private val topLevelScope: RaptorAssemblyInstallationScope,
) : RaptorComponent.Base<RaptorAggregateComponent<Aggregate, Id, Command, Change>>(RaptorDomainPlugin) {

	private val changeDefinitions: MutableMap<KClass<out Change>, RaptorAggregateChangeDefinition<Id, out Change>> = hashMapOf()
	private val commandDefinitions: MutableMap<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> = hashMapOf()
	private var projectionDefinition: RaptorAggregateProjectionDefinition<*, *, Change>? = null


	@RaptorDsl
	public fun change(discriminator: String, changeClass: KClass<out Change>) {
		check(!changeDefinitions.containsKey(changeClass)) { "Cannot define change $changeClass multiple times." }
		check(changeDefinitions.values.none { it.discriminator == discriminator }) {
			"Cannot define multiple changes with the same discriminator: $discriminator."
		}

		changeDefinitions[changeClass] = RaptorAggregateChangeDefinition(
			changeClass = changeClass,
			discriminator = discriminator,
		)
	}


	@RaptorDsl
	public fun command(commandClass: KClass<out Command>) {
		check(!commandDefinitions.containsKey(commandClass)) { "Cannot define command $commandClass multiple times." }

		commandDefinitions[commandClass] = RaptorAggregateCommandDefinition(commandClass = commandClass)
	}


	internal fun complete() =
		RaptorAggregateDefinition(
			aggregateClass = aggregateClass,
			changeClass = changeClass,
			changeDefinitions = changeDefinitions.values.toHashSet(),
			commandClass = commandClass,
			commandDefinitions = commandDefinitions.values.toHashSet(),
			discriminator = discriminator,
			factory = factory,
			idClass = idClass,
			projectionDefinition = projectionDefinition,
		)


	@RaptorInternalApi // FIXME
	@RaptorDsl
	public fun <Projection : RaptorAggregateProjection<Id>> _project(
		projectionClass: KClass<Projection>, projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, *, Change>,
	) {
		check(this.projectionDefinition == null) { "Cannot set multiple projector factories for aggregate $aggregateClass." }

		this.projectionDefinition =
			RaptorAggregateProjectionDefinition(
				factory = projectorFactory as () -> RaptorAggregateProjector.Incremental<RaptorAggregateProjection<RaptorAggregateProjectionId>, RaptorAggregateProjectionId, RaptorAggregateChange<RaptorAggregateProjectionId>>,
				idClass = idClass as KClass<RaptorAggregateProjectionId>,
				projectionClass = projectionClass as KClass<RaptorAggregateProjection<RaptorAggregateProjectionId>>,
			) as RaptorAggregateProjectionDefinition<*, *, Change>

		with(topLevelScope) { // FIXME remove hack
			optional(RaptorDIPlugin) {
//				transactions { // FIXME
				di.provide(RaptorAggregateProjectionLoader::class.createType(listOf(
					KTypeProjection.invariant(projectionClass.starProjectedType),
					KTypeProjection.invariant(this@RaptorAggregateComponent.idClass.starProjectedType),
				))) {
					// FIXME Improve.
					// FIXME (context as RaptorTransactionContext)
					context.projectionLoader(this@RaptorAggregateComponent.idClass)
				}
//				}
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
public fun <Event : RaptorAggregateChange<*>>
	RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, in Event>>.change(
	discriminator: String,
	eventClass: KClass<Event>,
) {
	each {
		change(discriminator, eventClass)
	}
}


@RaptorDsl
public inline fun <reified Event : RaptorAggregateChange<*>>
	RaptorAssemblyQuery<RaptorAggregateComponent<*, *, *, in Event>>.change(
	discriminator: String,
) {
	change(discriminator, Event::class)
}


@OptIn(RaptorInternalApi::class) // FIXME remove
@RaptorDsl
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId, Event : RaptorAggregateChange<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	projectionClass: KClass<Projection>,
	projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Event>,
) {
	each {
		_project(projectionClass, projectorFactory)
	}
}


@RaptorDsl
public inline fun <reified Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId, Event : RaptorAggregateChange<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	noinline projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Event>,
) {
	project(Projection::class, projectorFactory)
}
