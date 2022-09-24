package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateController<Id : RaptorAggregateId> {

	public fun execute(command: RaptorAggregateCommand<Id>): List<RaptorEvent<Id, RaptorAggregateEvent<Id>>>
	public fun handle(event: RaptorEvent<Id, RaptorAggregateEvent<Id>>)
}
