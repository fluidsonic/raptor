package io.fluidsonic.raptor.domain


public interface RaptorAggregateCommandExecutor {

	public fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>)
}
