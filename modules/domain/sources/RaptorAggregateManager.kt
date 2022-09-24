package io.fluidsonic.raptor.cqrs


internal interface RaptorAggregateManager { // FIXME tx

	suspend fun commit()
	fun <Id : RaptorAggregateId> execute(id: Id, command: RaptorAggregateCommand<Id>)
	suspend fun load()
}
