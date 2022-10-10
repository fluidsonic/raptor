package io.fluidsonic.raptor.domain


public interface RaptorAggregateController<Id : RaptorAggregateId> {

	public fun execute(command: RaptorAggregateCommand<Id>): List<RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>>
	public fun handle(event: RaptorAggregateEvent<Id, RaptorAggregateChange<Id>>)
}
