package io.fluidsonic.raptor.domain

import kotlin.reflect.*


public interface RaptorIndividualAggregateStoreFactory {

	public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<Id, Change>


	public companion object
}


public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>> RaptorIndividualAggregateStoreFactory.create(
	name: String,
): RaptorIndividualAggregateStore<Id, Change> =
	create(name = name, eventType = typeOf<RaptorAggregateEvent<Id, Change>>())
