package io.fluidsonic.raptor.domain


public sealed interface RaptorAggregateStreamMessage<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	> {

	public class BulkReplay(
		public val batches: List<RaptorAggregateEventBatch<*, *>>,
	) : RaptorAggregateStreamMessage<Nothing, Nothing>

	public object Loaded : RaptorAggregateStreamMessage<Nothing, Nothing>
	public data class Other(val value: Any) : RaptorAggregateStreamMessage<Nothing, Nothing>
}
