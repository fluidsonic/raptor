package io.fluidsonic.raptor.store


/**
 * Thrown by [RaptorKeyValueStore.update] when the decision could not be applied within the
 * allowed number of attempts because the value kept changing concurrently.
 *
 * @property maxAttempts the number of attempts that were made before giving up.
 */
public class RaptorOptimisticUpdateException(
	public val maxAttempts: Int,
) : RuntimeException("Optimistic update failed after $maxAttempts attempt(s) due to concurrent modifications.")
