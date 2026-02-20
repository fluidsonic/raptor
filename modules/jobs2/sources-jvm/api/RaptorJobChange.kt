package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*


/**
 * Domain changes (facts) representing what happened to a job.
 *
 * In Raptor DDD, changes are the counterpart of commands: a command expresses intent,
 * a change records the resulting fact. Changes use past tense (e.g., `Created`, `ExecutionStarted`).
 */
public sealed interface RaptorJobChange<out Input, out Output> {

	public data class Created<out Input, out Output>(
		public val description: RaptorJobDescription<out Input, out Output>,
		public val input: Input,
		public val timestamp: Timestamp,
	) : RaptorJobChange<Input, Output>

	public data class Canceled(
		public val timestamp: Timestamp,
	) : RaptorJobChange<Nothing, Nothing>

	public data class ExecutionCanceled(
		public val timestamp: Timestamp,
	) : RaptorJobChange<Nothing, Nothing>

	public data class ExecutionFailed(
		public val message: String? = null,
		public val retryable: Boolean = true,
		public val timestamp: Timestamp,
	) : RaptorJobChange<Nothing, Nothing>

	public data class ExecutionStarted(
		public val timestamp: Timestamp,
	) : RaptorJobChange<Nothing, Nothing>

	public data class ExecutionSucceeded<out Output>(
		public val output: Output,
		public val timestamp: Timestamp,
	) : RaptorJobChange<Nothing, Output>
}
