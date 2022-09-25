package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateCommandExecutor {

	public fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>)
}
