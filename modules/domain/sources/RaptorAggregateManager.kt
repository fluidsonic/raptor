package io.fluidsonic.raptor.domain


internal interface RaptorAggregateManager : RaptorAggregateCommandExecutor { // FIXME tx

	suspend fun commit()
	suspend fun load()
}
