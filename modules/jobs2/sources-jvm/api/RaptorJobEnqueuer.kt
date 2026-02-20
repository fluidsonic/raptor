package io.fluidsonic.raptor.jobs2


/**
 * Write operations for jobs (enqueue/cancel).
 */
public interface RaptorJobEnqueuer {

	public suspend fun cancel(id: RaptorJobId<*, *>)
	public suspend fun <Input, Output> enqueue(description: RaptorJobDescription<Input, Output>, input: Input): RaptorJobId<Input, Output>
}
