package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
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
	private val topLevelScope: RaptorAssemblyInstallationScope,
) : RaptorComponent.Base<RaptorAggregateComponent<Aggregate, Id, Command, Event>>(RaptorDomainPlugin) {

	private val commandDefinitions: MutableMap<KClass<out Command>, RaptorAggregateCommandDefinition<Id, out Command>> = hashMapOf()
	private val eventDefinitions: MutableMap<KClass<out Event>, RaptorAggregateEventDefinition<Id, out Event>> = hashMapOf()
	private var projectionDefinition: RaptorAggregateProjectionDefinition<*, *, Event>? = null


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
			projectionDefinition = projectionDefinition,
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
	public fun <Projection : RaptorAggregateProjection<Id>> _project(
		projectionClass: KClass<Projection>, projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, *, Event>,
	) {
		check(this.projectionDefinition == null) { "Cannot set multiple projector factories for aggregate $aggregateClass." }

		this.projectionDefinition =
			RaptorAggregateProjectionDefinition(
				factory = projectorFactory as () -> RaptorAggregateProjector.Incremental<RaptorAggregateProjection<RaptorAggregateProjectionId>, RaptorAggregateProjectionId, RaptorAggregateEvent<RaptorAggregateProjectionId>>,
				idClass = idClass as KClass<RaptorAggregateProjectionId>,
				projectionClass = projectionClass as KClass<RaptorAggregateProjection<RaptorAggregateProjectionId>>,
			) as RaptorAggregateProjectionDefinition<*, *, Event>

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
public fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId, Event : RaptorAggregateEvent<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	projectionClass: KClass<Projection>,
	projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Event>,
) {
	each {
		_project(projectionClass, projectorFactory)
	}
}


@RaptorDsl
public inline fun <reified Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId, Event : RaptorAggregateEvent<Id>>
	RaptorAssemblyQuery<RaptorAggregateComponent<out RaptorAggregate<Id, *, Event>, Id, *, Event>>.project(
	noinline projectorFactory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Event>,
) {
	project(Projection::class, projectorFactory)
}
