package io.fluidsonic.raptor.domain


public sealed interface RaptorAggregateProjectionStreamMessage<
	out ProjectionId : RaptorAggregateProjectionId,
	out Projection : RaptorProjection<ProjectionId>,
	out Change : RaptorAggregateChange<ProjectionId>,
	> {

	public object Loaded : RaptorAggregateProjectionStreamMessage<Nothing, Nothing, Nothing>
	public data class Other(val value: Any) : RaptorAggregateProjectionStreamMessage<Nothing, Nothing, Nothing>
}
