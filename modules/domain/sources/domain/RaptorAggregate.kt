package io.fluidsonic.raptor.cqrs


public interface RaptorAggregate<
	Id : RaptorAggregateId,
	in Command : RaptorAggregateCommand<Id>,
	Event : RaptorAggregateChange<Id>,
	> : RaptorEntity<Id> {

	public override val id: Id

	public fun execute(command: Command): List<Event>
	public fun handle(event: Event)
}
