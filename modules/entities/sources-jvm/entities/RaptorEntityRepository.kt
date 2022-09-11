package io.fluidsonic.raptor

import io.fluidsonic.raptor.ktor.*


public interface RaptorEntityRepository<out Entity : RaptorEntity, in Id : RaptorEntityId> : RaptorEntityResolver<Entity, Id> {

	public suspend fun queryOrNull(id: Id): Entity?
	public suspend fun queryOrSkip(ids: Iterable<Id>): Collection<Entity>


	// FIXME rework
	override suspend fun resolveOrNull(id: Id): Entity? =
		queryOrNull(id)


	override suspend fun resolveOrSkip(ids: Collection<Id>): Collection<Entity> =
		queryOrSkip(ids)
}


public suspend fun <Value : RaptorEntity, Id : RaptorEntityId> RaptorEntityRepository<Value, Id>.query(id: Id): Value =
	queryOrNull(id) ?: throw ServerFailure.ofUser(
		code = "not found",
		userMessage = "There is no entity '$id'." // FIXME i18n is done by library user
	)


@JvmName("queryOptional")
public suspend fun <Value : RaptorEntity, Id : RaptorEntityId> RaptorEntityRepository<Value, Id>.query(id: Id?): Value? =
	id?.let { query(id) }


@Suppress("NAME_SHADOWING")
public suspend fun <Value : RaptorEntity, Id : RaptorEntityId> RaptorEntityRepository<Value, Id>.query(ids: Iterable<Id>): List<Value> {
	val ids = ids.toList()
	val fetchedEntities = queryOrSkip(ids)

	return ids.map { id ->
		fetchedEntities.firstOrNull { it.id == id }
			?: throw throw ServerFailure.ofUser(
				code = "not found",
				userMessage = "There is no entity '$id'." // FIXME i18n is done by library user
			)
	}
}
