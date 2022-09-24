package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


internal class DefaultProjectionLoader<out Projection : RaptorProjection<Id>, Id : RaptorProjectionId> : RaptorProjectionLoader<Projection, Id> {

	override fun loadAll(): Flow<Projection> {
		TODO("Not yet implemented")
	}

	override suspend fun loadOrNull(id: Id): Projection? {
		TODO("Not yet implemented")
	}
}
