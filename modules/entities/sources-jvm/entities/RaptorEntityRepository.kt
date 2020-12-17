package io.fluidsonic.raptor


public interface RaptorEntityRepository<out Value : RaptorEntity, in Id : RaptorEntityId> {

	public suspend fun queryOrNull(id: Id): Value?
	public suspend fun queryOrSkip(ids: Iterable<Id>): Collection<Value>
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
