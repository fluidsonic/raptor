package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*


public data class RaptorDomain(
	public val aggregates: Aggregates,
) {

	public data class Aggregates(
		public val definitions: Set<RaptorAggregateDefinition<*, *, *, *>>,
		public val store: RaptorAggregateStore, // FIXME ok to have here?
	) {

		private val definitionsByIdClass: Map<KClass<out RaptorAggregateId>, RaptorAggregateDefinition<*, *, *, *>> =
			definitions.associateByTo(hashMapOf()) { it.idClass }


		public inline fun <reified Id : RaptorAggregateId>
			definition(): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
			definition(Id::class)


		public fun <Id : RaptorAggregateId>
			definition(id: Id): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, *, *>? =
			definition(id::class)


		@Suppress("UNCHECKED_CAST")
		public fun <Id : RaptorAggregateId> definition(
			idClass: KClass<Id>,
		): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
			definitionsByIdClass[idClass]
				as RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>?
	}
}
