package io.fluidsonic.raptor.domain


public sealed interface RaptorAggregateStreamMessage<
	out AggregateId : RaptorAggregateId,
	out Change : RaptorAggregateChange<AggregateId>,
	> {

	public object Loaded : RaptorAggregateStreamMessage<Nothing, Nothing>
	public data class Other(val value: Any) : RaptorAggregateStreamMessage<Nothing, Nothing>
}
