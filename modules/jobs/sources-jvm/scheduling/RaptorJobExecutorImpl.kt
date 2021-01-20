package io.fluidsonic.raptor


internal class RaptorJobExecutorImpl<Data>(
	override val group: RaptorJobGroup<Data>,
	override val execute: suspend RaptorContext.(data: Data) -> Unit,
) : RaptorJobExecutor<Data>
