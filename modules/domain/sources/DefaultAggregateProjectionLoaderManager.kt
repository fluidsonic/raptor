package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


internal class DefaultAggregateProjectionLoaderManager(
	definitions: Collection<RaptorAggregateProjectionDefinition<*, *, *>>,
) : RaptorAggregateProjectorLoaderManager {

	private val factoriesByClass: Map<KClass<out RaptorAggregateProjectionId>, (() -> RaptorAggregateProjector.Incremental<RaptorAggregateProjection<*>, out RaptorAggregateProjectionId, *>)?> =
		definitions.associate { it.idClass to it.factory }

	// FIXME concurrent
	private val loaders: MutableMap<KClass<out RaptorAggregateProjectionId>, RaptorAggregateProjectionLoader<*, *>> = hashMapOf()


	internal fun addEvent(event: RaptorEvent<*, *>) {
		val id = event.aggregateId as? RaptorAggregateProjectionId ?: return

		// FIXME
		(getOrCreate(id::class)
			as DefaultAggregateProjectionLoader<RaptorAggregateProjection<RaptorAggregateProjectionId>, RaptorAggregateProjectionId, RaptorAggregateEvent<RaptorAggregateProjectionId>>)
			.addEvent(event as RaptorEvent<RaptorAggregateProjectionId, *>)
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Projection : RaptorAggregateProjection<Id>, Id : RaptorAggregateProjectionId> getOrCreate(
		idClass: KClass<Id>, // FIXME use both, projection class and id class?
	): RaptorAggregateProjectionLoader<Projection, Id> =
		loaders.getOrPut(idClass) {
			val factory = factoriesByClass[idClass]
				as (() -> RaptorAggregateProjector.Incremental<Projection, Id, *>)?
				?: error("No projection factory registered for projection ID $idClass.")

			DefaultAggregateProjectionLoader(factory = factory)
		} as RaptorAggregateProjectionLoader<Projection, Id>
}
