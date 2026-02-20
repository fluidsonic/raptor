package io.fluidsonic.raptor.jobs2

import kotlinx.coroutines.flow.*


/**
 * Read/stream operations for jobs.
 */
public interface RaptorJobFetcher {

	public suspend fun <Input, Output> fetchOrNull(id: RaptorJobId<Input, Output>): RaptorJob<Input, Output>?
	public fun <Input, Output> find(descriptionId: JobDescriptionId<Input, Output>, input: Input): Flow<RaptorJob<Input, Output>>
	public fun <Input, Output> follow(id: RaptorJobId<Input, Output>): Flow<RaptorJobUpdate<Input, Output>>
}


/**
 * A snapshot of a job along with the change that produced it, emitted by [RaptorJobFetcher.follow].
 *
 * The [change] is `null` for the initial snapshot.
 */
public data class RaptorJobUpdate<out Input, out Output>(
	public val job: RaptorJob<Input, Output>,
	public val change: RaptorJobChange<Input, Output>?,
)
