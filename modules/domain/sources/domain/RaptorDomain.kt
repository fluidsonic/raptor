package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorDomain(
	public val aggregateDefinitions: Set<RaptorAggregateDefinition<*, *, *, *>>,
) {

	private val aggregateDefinitionsByIdClass: Map<KClass<out RaptorAggregateId>, RaptorAggregateDefinition<*, *, *, *>> =
		aggregateDefinitions.associateByTo(hashMapOf()) { it.idClass }


	public inline fun <reified Id : RaptorAggregateId>
		aggregateDefinition(): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
		aggregateDefinition(Id::class)


	public fun <Id : RaptorAggregateId>
		aggregateDefinition(id: Id): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, *, *>? =
		aggregateDefinition(id::class)


	@Suppress("UNCHECKED_CAST")
	public fun <Id : RaptorAggregateId> aggregateDefinition(
		idClass: KClass<Id>,
	): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
		aggregateDefinitionsByIdClass[idClass]
			as RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>?
}
