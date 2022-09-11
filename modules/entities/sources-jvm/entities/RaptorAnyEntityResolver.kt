package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.reflect.*


// FIXME rework
internal class RaptorAnyEntityResolver(
	context: RaptorContext,
	resolverTypes: Map<KClass<out RaptorEntityId>, KType>,
) : RaptorEntityResolver<RaptorEntity, RaptorEntityId> {

	@Suppress("UNCHECKED_CAST")
	private val resolversByIdClass = run {
		val di = context.di

		resolverTypes.mapValues { (_, resolverType) ->
			di.get(resolverType) as RaptorEntityResolver<RaptorEntity, RaptorEntityId>
		}
	}


	override suspend fun resolveOrNull(id: RaptorEntityId): RaptorEntity? =
		resolverByIdClass(id::class).resolveOrNull(id)


	override suspend fun resolveOrSkip(ids: Collection<RaptorEntityId>): Collection<RaptorEntity> =
		ids
			.groupBy { it::class }
			.flatMap { (idClass, ids) -> resolverByIdClass(idClass).resolveOrSkip(ids) }


	private fun resolverByIdClass(idClass: KClass<out RaptorEntityId>): RaptorEntityResolver<RaptorEntity, RaptorEntityId> =
		resolversByIdClass[idClass]
			?: error("No entity resolver was provided for '${idClass.qualifiedName}'.")
}
