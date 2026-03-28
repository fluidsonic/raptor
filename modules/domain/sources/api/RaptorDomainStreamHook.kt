package io.fluidsonic.raptor.domain


public interface RaptorDomainStreamHook {

	public fun onAggregateEvent(event: RaptorAggregateEvent<*, *>) {}
	public fun onAggregateProjectionEvent(event: RaptorAggregateProjectionEvent<*, *, *>) {}
	public fun onReplayCompleted() {}
}
