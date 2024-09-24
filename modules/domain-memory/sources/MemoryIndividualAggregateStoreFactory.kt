package io.fluidsonic.raptor.domain.memory

import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


private object MemoryIndividualAggregateStoreFactory : RaptorIndividualAggregateStoreFactory {

	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<Id, Change> =
		RaptorIndividualAggregateStore.memory()
}


public fun RaptorIndividualAggregateStoreFactory.Companion.memory(): RaptorIndividualAggregateStoreFactory =
	MemoryIndividualAggregateStoreFactory
