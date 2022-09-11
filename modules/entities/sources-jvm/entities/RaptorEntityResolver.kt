package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


public interface RaptorEntityResolver<out Value : RaptorEntity, in Id : RaptorEntityId> {

	public suspend fun resolveOrNull(id: Id): Value?
	public suspend fun resolveOrSkip(ids: Collection<Id>): Collection<Value>
}


public val RaptorScope.entityResolver: RaptorEntityResolver<RaptorEntity, RaptorEntityId>
	get() = di.get()
