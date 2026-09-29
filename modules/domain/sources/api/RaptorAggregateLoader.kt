package io.fluidsonic.raptor.domain

import io.fluidsonic.time.Timestamp
import kotlinx.coroutines.flow.*


public interface RaptorAggregateLoader {

	/**
	 * The timestamp of the most recently added event across all aggregates, or `null` if the store
	 * is empty.
	 */
	public suspend fun lastEventTimestampOrNull(): Timestamp?

	/**
	 * Streams every event of every aggregate in the store, ordered ascending by event ID.
	 *
	 * [after] is exclusive: `null` returns the full history, otherwise only events with an ID
	 * greater than [after] are returned.
	 *
	 * Whether a concurrent write is visible in an already-returned [Flow] is implementation-defined:
	 * a caller must not rely on either seeing or not seeing it.
	 */
	public fun load(after: RaptorAggregateEventId? = null): Flow<RaptorAggregateEvent<*, *>>

	/**
	 * Streams the events of a single aggregate identified by [id] (of the given [definition]),
	 * ordered ascending by `version` — identical to event-id order for a single aggregate, since
	 * per-aggregate versions are contiguous and event IDs are globally monotonic.
	 *
	 * [afterVersion] is exclusive: `null` returns the full history, otherwise only events with
	 * `version > afterVersion` are returned; an [id] with no stored events (or none past
	 * [afterVersion]) yields an empty flow rather than throwing. A consumer that stops collecting
	 * mid-stream should resume with `afterVersion` set to an event's `version` only once that
	 * event's `version == lastVersionInBatch`, to stay aligned with commit batch boundaries.
	 *
	 * Whether a concurrent write is visible in an already-returned [Flow] is implementation-defined
	 * — some implementations snapshot at call time, others query lazily at collection time — the
	 * same property as [load]: a caller must not rely on either behavior.
	 *
	 * @throws IllegalArgumentException if [id] is not exactly an instance of `definition.idClass`.
	 * @throws IllegalStateException if `definition.isIndividual` — those aggregates are stored
	 *   separately; use [RaptorIndividualAggregateStore] instead.
	 */
	public fun <Id : RaptorAggregateId> loadAggregate(
		definition: RaptorAggregateDefinition<*, out Id, *, *>,
		id: Id,
		afterVersion: Int? = null,
	): Flow<RaptorAggregateEvent<*, *>>

	/**
	 * Streams one page of events matching [changes], ordered by event ID: newest first when
	 * [descending] (the default), oldest first otherwise.
	 *
	 * [changes] maps aggregate definitions to the change definitions of that aggregate to include; a
	 * `null` value includes all changes of that aggregate, a `null` map disables filtering, and an
	 * empty map matches nothing. Change discriminators are only unique within an aggregate, which is
	 * why changes are always paired with their aggregate.
	 *
	 * [before] and [after] are exclusive event-ID bounds, and either or both may be set:
	 * - page backwards: descending, before = last ID of the previous page
	 * - page forwards: ascending, after = last ID of the previous page
	 * - range: both set
	 *
	 * At most [limit] events are returned; fewer than [limit] means the end of the range was
	 * reached. Event IDs are global, unique and monotonic, so the order is total. An ID used as a
	 * cursor stays valid while new events are added at the head: they only ever get greater IDs.
	 *
	 * Individual aggregates are not included; they are stored separately.
	 * Events are fully decoded, the same as [load].
	 *
	 * Arguments are validated when this function is called, not when the [Flow] is collected.
	 *
	 * @throws IllegalArgumentException if [limit] is not in `1..MAX_PAGE_SIZE`, if [before] ≤ [after]
	 *   when both are set, or if a change definition does not belong to its aggregate definition.
	 * @throws IllegalStateException if [changes] contains an individual aggregate.
	 */
	public fun loadPage(
		limit: Int,
		changes: Map<RaptorAggregateDefinition<*, *, *, *>, Set<RaptorAggregateChangeDefinition<*, *>>?>? = null,
		before: RaptorAggregateEventId? = null,
		after: RaptorAggregateEventId? = null,
		descending: Boolean = true,
	): Flow<RaptorAggregateEvent<*, *>>


	public companion object {

		public const val MAX_PAGE_SIZE: Int = 1_000
	}
}
