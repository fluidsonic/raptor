package io.fluidsonic.raptor.domain


internal data class RaptorAggregateEventBatch<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val events: List<RaptorAggregateEvent<AggregateId, Change>>,
	val version: Int,
) {

	init {
		require(events.isNotEmpty()) { "'events' must not be empty." }
		require(events.all { it.aggregateId == aggregateId }) { "'events' must all have the same 'aggregateId' as the batch: $this" }
		// We're getting rid of batching anyway…
//		require(events.all { it.lastVersionInBatch == version }) { "'events' must all have the same 'lastVersionInBatch' as the batch 'version': $this" }
	}
}
