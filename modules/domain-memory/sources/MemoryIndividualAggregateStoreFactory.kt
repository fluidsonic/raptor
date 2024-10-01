package io.fluidsonic.raptor.domain.memory

import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


private object MemoryIndividualAggregateStoreFactory : RaptorIndividualAggregateStoreFactory {

	override fun create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<*, *> =
		RaptorIndividualAggregateStore.memory()
}


public fun RaptorIndividualAggregateStoreFactory.Companion.memory(): RaptorIndividualAggregateStoreFactory =
	MemoryIndividualAggregateStoreFactory
