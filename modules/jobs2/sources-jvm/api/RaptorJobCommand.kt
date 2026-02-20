package io.fluidsonic.raptor.jobs2

import io.fluidsonic.time.*


/**
 * Commands to mutate job state.
 *
 * In Raptor DDD, commands express intent using imperative verbs (e.g., `Create`, `Cancel`).
 * Each command has a corresponding [RaptorJobChange] that records the resulting fact.
 */
public sealed interface RaptorJobCommand<out Input, out Output> {

	public data class Create<out Input, out Output>(
		public val description: RaptorJobDescription<out Input, out Output>,
		public val input: Input,
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Input, Output>

	public data class Cancel(
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Nothing, Nothing>

	public data class CancelExecution(
		public val id: JobExecutionId,
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Nothing, Nothing>

	public data class FailExecution(
		public val message: String? = null,
		public val retryable: Boolean = true,
		public val id: JobExecutionId,
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Nothing, Nothing>

	public data class StartExecution(
		public val id: JobExecutionId,
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Nothing, Nothing>

	public data class SucceedExecution<out Output>(
		public val id: JobExecutionId,
		public val output: Output,
		public val timestamp: Timestamp,
	) : RaptorJobCommand<Nothing, Output>
}
