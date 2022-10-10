package io.fluidsonic.raptor.cqrs


public interface RaptorAggregateController<Id : RaptorAggregateId> {

	public fun execute(command: RaptorAggregateCommand<Id>): List<RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>>
	public fun handle(event: RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>)
}
