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
}
