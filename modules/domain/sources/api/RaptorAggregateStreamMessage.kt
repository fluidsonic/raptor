package io.fluidsonic.raptor.domain


public sealed interface RaptorAggregateStreamMessage<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	> {

	public data object Loaded : RaptorAggregateStreamMessage<Nothing, Nothing>
	public data class Other(val value: Any) : RaptorAggregateStreamMessage<Nothing, Nothing>

	public class Replay(
		public val batches: List<RaptorAggregateEventBatch<*, *>>,
	) : RaptorAggregateStreamMessage<Nothing, Nothing>
}
