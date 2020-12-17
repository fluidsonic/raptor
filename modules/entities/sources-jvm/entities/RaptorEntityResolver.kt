package io.fluidsonic.raptor


public interface RaptorEntityResolver<out Value : RaptorEntity, in Id : RaptorEntityId> {

	public suspend fun resolveOrNull(id: Id): Value?
	public suspend fun resolveOrSkip(ids: Collection<Id>): Collection<Value>
}


public val RaptorTransactionContext.entityResolver: RaptorEntityResolver<RaptorEntity, RaptorEntityId>
	get() = di.get()
