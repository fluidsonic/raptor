package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*


/**
 * A single logical work unit that may have multiple executions (retries).
 * The last execution determines the final outcome.
 */
public interface RaptorJob<out Input, out Output> {

	public val cancellationRequestTimestamp: Timestamp?
	public val creationTimestamp: Timestamp
	public val description: RaptorJobDescription<out Input, out Output>
	public val executions: List<RaptorJobExecution<Output>>
	public val id: RaptorJobId<out Input, out Output>
	public val input: Input
}


/**
 * Returns the output of the job if the last execution succeeded, or `null` otherwise.
 */
public fun <Output> RaptorJob<*, Output>.outputOrNull(): Output? =
	when (val execution = executions.lastOrNull()?.status) {
		is RaptorJobExecutionStatus.Succeeded -> execution.output
		else -> null
	}


/**
 * Derives an overall job status from cancellation state and the last execution.
 */
public val <Output> RaptorJob<*, Output>.status: RaptorJobStatus<Output>
	get() = when (val timestamp = cancellationRequestTimestamp) {
		null -> when (val status = executions.lastOrNull()?.status) {
			null -> RaptorJobStatus.Pending
			is RaptorJobExecutionStatus.Canceled -> error("Job $id has a canceled execution but no cancellation request.")
			is RaptorJobExecutionStatus.Failed -> RaptorJobStatus.Failed(status)
			is RaptorJobExecutionStatus.Succeeded -> RaptorJobStatus.Succeeded(status)
			is RaptorJobExecutionStatus.Started -> RaptorJobStatus.Running(status)
		}

		else -> when (val status = executions.lastOrNull()?.status) {
			null -> RaptorJobStatus.Canceled(timestamp = timestamp)
			is RaptorJobExecutionStatus.Canceled -> RaptorJobStatus.Canceled(timestamp = status.timestamp.coerceAtLeast(timestamp))
			is RaptorJobExecutionStatus.Failed -> RaptorJobStatus.Canceled(timestamp = status.timestamp.coerceAtLeast(timestamp))
			is RaptorJobExecutionStatus.Started -> RaptorJobStatus.Canceling
			is RaptorJobExecutionStatus.Succeeded -> RaptorJobStatus.Succeeded(status)
		}
	}


/**
 * High-level job status derived from cancellation state and the last execution.
 */
public sealed interface RaptorJobStatus<out Output> {

	public data class Canceled(val timestamp: Timestamp) : Ended<Nothing>

	public data object Canceling : RaptorJobStatus<Nothing>

	public data class Failed(
		val executionStatus: RaptorJobExecutionStatus.Failed,
	) : Ended<Nothing>

	public data object Pending : RaptorJobStatus<Nothing>

	public data class Running(
		val executionStatus: RaptorJobExecutionStatus.Started,
	) : RaptorJobStatus<Nothing>

	public data class Succeeded<out Output>(
		val executionStatus: RaptorJobExecutionStatus.Succeeded<Output>,
	) : Ended<Output>

	/**
	 * Terminal state of a job. A job that has ended will not change status again.
	 */
	public sealed interface Ended<out Output> : RaptorJobStatus<Output>
}
