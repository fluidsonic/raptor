package io.fluidsonic.raptor.cqrs


internal interface RaptorAggregateManager : RaptorAggregateCommandExecutor { // FIXME tx

	suspend fun commit()
	suspend fun load()
}
