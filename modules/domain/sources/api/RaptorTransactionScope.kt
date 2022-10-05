package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


@RaptorDsl
public fun <Id : RaptorAggregateId> RaptorTransactionScope.execute(id: Id, command: RaptorAggregateCommand<Id>) {
	context.aggregateManager.execute(id, command)
}


// FIXME rm
@RaptorDsl
public suspend fun RaptorTransactionScope.commit() {
	context.aggregateManager.commit()
}
