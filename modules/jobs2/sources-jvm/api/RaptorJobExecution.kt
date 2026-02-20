package io.fluidsonic.raptor.jobs2

import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.*


/**
 * A single attempt to execute a job.
 *
 * A job may have multiple executions (retries). Executions are strictly ordered by [startTimestamp],
 * and only the last execution may be in a running state.
 */
public interface RaptorJobExecution<out Output> {

	public val id: JobExecutionId
	public val startTimestamp: Timestamp
	public val status: RaptorJobExecutionStatus<Output>
}


/**
 * Status of a single job execution attempt.
 */
public sealed interface RaptorJobExecutionStatus<out Output> {

	public data class Canceled(
		override val timestamp: Timestamp,
	) : Ended<Nothing>

	public data class Failed(
		public val message: String? = null,
		public val retryable: Boolean = true,
		override val timestamp: Timestamp,
	) : Ended<Nothing>

	public data object Started : RaptorJobExecutionStatus<Nothing>

	public data class Succeeded<out Output>(
		public val output: Output,
		override val timestamp: Timestamp,
	) : Ended<Output>

	/**
	 * Terminal state of an execution. All ended statuses carry the [timestamp] at which the execution ended.
	 */
	public sealed interface Ended<out Output> : RaptorJobExecutionStatus<Output> {

		public val timestamp: Timestamp
	}
}


/**
 * Identifier for a specific job execution attempt.
 */
@JvmInline
public value class JobExecutionId(private val value: String) : RaptorEntityId {

	override fun toString(): String =
		value
}
