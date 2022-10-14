package io.fluidsonic.raptor.domain


internal interface RaptorAggregateManager : RaptorAggregateCommandExecutor { // FIXME tx

	override suspend fun commit()
	suspend fun load()
}
