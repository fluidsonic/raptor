package io.fluidsonic.raptor.domain


public interface RaptorDomainStreamHook {

	public fun onAggregateStreamMessage(message: RaptorAggregateStreamMessage<*, *>) {}
	public fun onAggregateProjectionStreamMessage(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {}
}
