package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


public class RaptorAggregateDefinitions internal constructor(
	private val values: Set<RaptorAggregateDefinition<*, *, *, *>>,
) : Set<RaptorAggregateDefinition<*, *, *, *>> by values {

	private val definitionsByIdClass: Map<KClass<out RaptorAggregateId>, RaptorAggregateDefinition<*, *, *, *>> =
		values.associateByTo(hashMapOf()) { it.idClass }


	override fun equals(other: Any?): Boolean =
		this === other || (other is RaptorAggregateDefinitions && values == other.values)


	public inline fun <reified Id : RaptorAggregateId>
		get(): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
		get(Id::class)


	public operator fun <Id : RaptorAggregateId>
		get(id: Id): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, *, *>? =
		get(id::class)


	@Suppress("UNCHECKED_CAST")
	public operator fun <Id : RaptorAggregateId> get(
		idClass: KClass<Id>,
	): RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>? =
		definitionsByIdClass[idClass]
			as RaptorAggregateDefinition<out RaptorAggregate<out Id, *, *>, out Id, out RaptorAggregateCommand<Id>, *>?


	override fun hashCode(): Int =
		values.hashCode()


	override fun toString(): String =
		values.toString()
}
