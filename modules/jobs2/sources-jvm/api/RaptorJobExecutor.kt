package io.fluidsonic.raptor.jobs2


/**
 * Context provided to job executors during job execution.
 *
 * This is the context parameter for [RaptorJobExecutor.execute], providing access to runtime services
 * needed for job processing (e.g., DI, logging, tracing). Will be populated by the execution engine
 * when it is implemented.
 */
public interface RaptorJobExecutionContext


/**
 * Executes a job instance and returns its output.
 */
public interface RaptorJobExecutor<Input, Output> {

	context(_: RaptorJobExecutionContext)
	public suspend fun execute(job: RaptorJob<Input, Output>): Output
}
