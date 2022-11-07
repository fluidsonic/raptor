package io.fluidsonic.raptor.domain


public data class RaptorAggregateEventBatch<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	>(
	val aggregateId: AggregateId,
	val events: List<RaptorAggregateEvent<AggregateId, Change>>,
	val version: Int,
) : RaptorAggregateStreamMessage<AggregateId, Change> {

	init {
		require(events.isNotEmpty()) { "'events' must not be empty." }
		require(events.all { it.aggregateId == aggregateId }) { "'events' must all have the same 'aggregateId' as the batch: $this" }
		require(events.all { it.lastVersionInBatch == version }) { "'events' must all have the same 'lastVersionInBatch' as the batch 'version': $this" }
	}
}
