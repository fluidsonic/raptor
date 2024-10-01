package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


public interface RaptorIndividualAggregateManager<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> {

	public suspend fun commit(id: Id, expectedVersion: Int, changes: List<Change>, timestamp: Timestamp): List<RaptorAggregateEvent<Id, Change>>
	public suspend fun fetch(id: Id): List<RaptorAggregateEvent<Id, Change>>


	public companion object
}
