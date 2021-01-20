package io.fluidsonic.raptor


public interface RaptorJobExecutor<Data> {

	public val group: RaptorJobGroup<Data>
	public val execute: suspend RaptorContext.(data: Data) -> Unit
}


public fun <Data> RaptorJobGroup<Data>.executor(
	execute: suspend RaptorContext.(data: Data) -> Unit,
): RaptorJobExecutor<Data> =
	RaptorJobExecutorImpl(group = this, execute = execute)
