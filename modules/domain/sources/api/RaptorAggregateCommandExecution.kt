package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


public interface RaptorAggregateCommandExecution {

	@RaptorDsl
	public suspend fun commit()

	@RaptorDsl
	public fun <Id : RaptorAggregateId> execute(id: Id, version: Int?, command: RaptorAggregateCommand<Id>)
}


@RaptorDsl
public fun <Id : RaptorAggregateId> RaptorAggregateCommandExecution.execute(id: Id, command: RaptorAggregateCommand<Id>) {
	execute(id = id, version = null, command = command)
}
