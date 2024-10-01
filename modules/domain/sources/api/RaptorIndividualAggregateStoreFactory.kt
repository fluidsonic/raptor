package io.fluidsonic.raptor.domain

import kotlin.reflect.*


public interface RaptorIndividualAggregateStoreFactory {

	public fun create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<*, *>


	public companion object
}


@Suppress("UNCHECKED_CAST")
public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>> RaptorIndividualAggregateStoreFactory.create(
	name: String,
): RaptorIndividualAggregateStore<Id, Change> =
	create(name = name, eventType = typeOf<RaptorAggregateEvent<Id, Change>>())
		as RaptorIndividualAggregateStore<Id, Change>
