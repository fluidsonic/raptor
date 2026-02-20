package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.domain.*


/**
 * Middleware that intercepts aggregate events synchronously before service handlers.
 *
 * Implementations must be thread-safe as they may be called from multiple coroutines.
 * Middleware processes events during aggregate replay and live updates.
 *
 * Use cases:
 * - Index services that build in-memory lookups
 * - Validation that must complete before services see events
 * - Enrichment that adds data to events before service processing
 *
 * Example:
 * ```kotlin
 * internal class AdminIndexMiddleware(
 *     private val loader: RaptorAggregateProjectionLoader<Admin, AdminId>,
 * ) : AdminIndex, RaptorStreamMiddleware {
 *
 *     private val idsByEmailAddress = ConcurrentHashMap<String, AdminId>()
 *
 *     override suspend fun intercept(event: RaptorAggregateProjectionEvent<*, *, *>) {
 *         val change = event.change
 *         if (change !is AdminChange) return
 *
 *         when (change) {
 *             is AdminChange.Created -> {
 *                 val admin = checkNotNull(event.projection) as Admin
 *                 idsByEmailAddress[admin.emailAddress.lowercase()] = admin.id
 *             }
 *             // ... handle other changes
 *             else -> {}
 *         }
 *     }
 * }
 * ```
 */
public interface RaptorStreamMiddleware {

	/**
	 * Called for each aggregate projection event.
	 *
	 * Implementations should filter events by checking the change type
	 * and returning early if not relevant.
	 *
	 * This method is called synchronously before any service handlers
	 * process the event, ensuring indexes are up-to-date when services
	 * query them.
	 */
	public suspend fun intercept(event: RaptorAggregateProjectionEvent<*, *, *>)
}
