package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object FieldResolver : GFieldResolver<Any> {

	@Suppress("UNCHECKED_CAST")
	override suspend fun GFieldResolverContext.resolveField(parent: Any): Any? {
		val value = next()
			?: return null

		val scope = execution.raptorContext?.asScope()
			?: return null

		val aliasDefinition = fieldDefinition.raptorTypeDefinition as? GraphAliasDefinition<Any, Any>
			?: return value

		return scope.serializeAliasValue(value, serialize = aliasDefinition.serialize, typeRef = fieldDefinition.type)
	}


	// FIXME Consolidate list handling
	private fun RaptorGraphScope.serializeAliasValue(value: Any, serialize: RaptorGraphScope.(value: Any) -> Any?, typeRef: GTypeRef): Any? =
		when (typeRef) {
			is GListTypeRef -> when (value) {
				is Iterable<*> -> value.map { element ->
					element?.let { serializeAliasValue(it, serialize = serialize, typeRef = typeRef.elementType) }
				}
				else -> serializeAliasValue(value, serialize = serialize, typeRef = typeRef.elementType)
			}
			is GNamedTypeRef -> serialize(value)
			is GNonNullTypeRef -> serializeAliasValue(value, serialize = serialize, typeRef = typeRef.nullableRef)
		}
}
