package io.fluidsonic.raptor.domain


public interface RaptorAggregate<
	Id : RaptorAggregateId,
	in Command : RaptorAggregateCommand<Id>,
	Change : RaptorAggregateChange<Id>,
	> : RaptorEntity<Id> {

	public override val id: Id

	public fun copy(): RaptorAggregate<Id, Command, Change>
	public fun execute(command: Command): List<Change>
	public fun handle(change: Change)
}
