package io.fluidsonic.raptor.domain

import kotlin.reflect.*


public interface RaptorDomainStreamHook {

	/**
	 * Restricts which aggregates this hook's [onAggregateStreamMessage] is called for, by ID class.
	 *
	 * Read **once**, when the aggregate manager is constructed — changing this property afterward has no
	 * effect. Matching is is-a: a declared class matches every registered aggregate whose ID class is that
	 * class or a subtype of it, so a sealed supertype of several aggregate ID types may be declared here.
	 * `null` (the default) means no filter — the hook receives every aggregate's messages, exactly as before
	 * this property existed. `emptySet()` means the hook receives no aggregate messages at all.
	 *
	 * A declared class that matches no registered aggregate ID class, or that matches only individual
	 * aggregates (which never dispatch to hooks), fails during that same construction, naming the hook and
	 * the class.
	 *
	 * [RaptorAggregateStreamMessage.Loaded] is always delivered regardless of this filter.
	 *
	 * For an aggregate with a projection, its projection definition's ID class equals its own ID class —
	 * [RaptorAggregateProjectionId] extends [RaptorAggregateId] — so the same class may appear in both this
	 * filter and [projectionIdClassFilter] at once. That's expected: the two filters don't compete over a
	 * shared range: they independently gate two different channels ([onAggregateStreamMessage] vs.
	 * [onAggregateProjectionStreamMessage]) for that aggregate.
	 */
	public val aggregateIdClassFilter: Set<KClass<out RaptorAggregateId>>?
		get() = null

	/**
	 * Restricts which projections this hook's [onAggregateProjectionStreamMessage] is called for, by
	 * projection ID class. Same semantics as [aggregateIdClassFilter], applied to the projection channel
	 * instead of the aggregate channel. [RaptorAggregateProjectionStreamMessage.Loaded] is always delivered
	 * regardless of this filter.
	 */
	public val projectionIdClassFilter: Set<KClass<out RaptorAggregateProjectionId>>?
		get() = null


	public fun onAggregateStreamMessage(message: RaptorAggregateStreamMessage<*, *>) {}
	public fun onAggregateProjectionStreamMessage(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {}
}
