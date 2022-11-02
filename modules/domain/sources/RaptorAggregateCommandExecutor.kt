package io.fluidsonic.raptor.domain


public interface RaptorAggregateCommandExecutor {

	public suspend fun commit() // FIXME Not here
	public suspend fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>) // FIXME version
}
