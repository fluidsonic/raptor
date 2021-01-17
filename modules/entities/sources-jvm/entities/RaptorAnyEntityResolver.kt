//package io.fluidsonic.raptor
//
//import io.fluidsonic.stdlib.*
//import kotlin.reflect.*
//
//
//internal class RaptorAnyEntityResolver(
//	definitions: Collection<RaptorEntityIdDefinition<*>>,
//	context: RaptorContext,
//) : RaptorEntityResolver<RaptorEntity, RaptorEntityId> {
//
//	@Suppress("UNCHECKED_CAST")
//	private val resolversByType = run {
//		val di = context.di
//
//		definitions.associate { definition ->
//			definition.type to di { get(definition.resolverType) as RaptorEntityResolver<RaptorEntity, RaptorEntityId> }
//		}
//	}
//
//
//	override suspend fun resolveOrNull(id: RaptorEntityId): RaptorEntity? =
//		resolverByIdClass(id::class).resolveOrNull(id)
//
//
//	override suspend fun resolveOrSkip(ids: Collection<RaptorEntityId>): Collection<RaptorEntity> =
//		ids
//			.groupBy { it::class }
//			.flatMap { (idClass, ids) -> resolverByIdClass(idClass).resolveOrSkip(ids) }
//
//
//	private fun resolverByIdClass(idClass: KClass<out RaptorEntityId>): RaptorEntityResolver<RaptorEntity, RaptorEntityId> =
//		resolversByType[idClass]
//			.ifNull { error("No definition was provided for entity ID type '${idClass.qualifiedName}'.") }
//			.value
//}
