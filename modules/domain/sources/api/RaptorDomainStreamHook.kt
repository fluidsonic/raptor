package io.fluidsonic.raptor.domain

import kotlin.reflect.*


public interface RaptorDomainStreamHook {

	/**
	 * Restricts which aggregates this hook's [onAggregateEvent] is called for, by ID class.
	 *
	 * Read **once**, when the aggregate manager is constructed — changing this property afterward has no
	 * effect. Matching is is-a: a declared class matches every registered aggregate whose ID class is that
	 * class or a subtype of it, so a sealed supertype of several aggregate ID types may be declared here.
	 * `null` (the default) means no filter — the hook receives every aggregate's events, exactly as before
	 * this property existed. `emptySet()` means the hook receives no aggregate events at all.
	 *
	 * A declared class that matches no registered aggregate ID class, or that matches only individual
	 * aggregates (which never dispatch to hooks), fails during that same construction, naming the hook and
	 * the class.
	 *
	 * [onReplayCompleted] is always called regardless of this filter.
	 *
	 * For an aggregate with a projection, its projection definition's ID class equals its own ID class —
	 * [RaptorAggregateProjectionId] extends [RaptorAggregateId] — so the same class may appear in both this
	 * filter and [projectionIdClassFilter] at once. That's expected: the two filters don't compete over a
	 * shared range: they independently gate two different channels ([onAggregateEvent] vs.
	 * [onAggregateProjectionEvent]) for that aggregate.
	 */
	public val aggregateIdClassFilter: Set<KClass<out RaptorAggregateId>>?
		get() = null

	/**
	 * Restricts which projections this hook's [onAggregateProjectionEvent] is called for, by
	 * projection ID class. Same semantics as [aggregateIdClassFilter], applied to the projection channel
	 * instead of the aggregate channel. [onReplayCompleted] is always called regardless of this filter.
	 */
	public val projectionIdClassFilter: Set<KClass<out RaptorAggregateProjectionId>>?
		get() = null


	public fun onAggregateEvent(event: RaptorAggregateEvent<*, *>) {}
	public fun onAggregateProjectionEvent(event: RaptorAggregateProjectionEvent<*, *, *>) {}
	public fun onReplayCompleted() {}
}
