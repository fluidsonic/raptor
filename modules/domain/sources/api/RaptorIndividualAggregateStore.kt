package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


public interface RaptorIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> {

	public suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>>
	public suspend fun preload()
	public suspend fun save(id: Id, expectedVersion: Int, changes: List<Change>, timestamp: Timestamp)


	public companion object
}
