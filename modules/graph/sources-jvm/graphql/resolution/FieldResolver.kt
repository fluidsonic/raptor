package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal object FieldResolver : GFieldResolver<Any> {

	override suspend fun GFieldResolverContext.resolveField(parent: Any): Any? {
		val context = checkNotNull(execution.raptorContext)
		val field = checkNotNull(fieldDefinition.raptorField) as GraphField.Resolvable
		val resolve = checkNotNull(field.resolve)
		val argumentResolver = checkNotNull(field.argumentResolver)

		val outputScope = object : RaptorGraphOutputScope, RaptorGraphScope by context {}  // FIXME improve

		val value = argumentResolver.withArguments(
			argumentValues = arguments,
			argumentDefinitions = fieldDefinition.argumentDefinitions,
			context = execution
		) { resolve(outputScope, parent) }
			?: return null

		val aliasType = fieldDefinition.raptorType as? AliasGraphType
			?: return value

		return outputScope.serializeAliasValue(value, serialize = aliasType.convertAliasToReferenced, typeRef = fieldDefinition.type)
	}


	// FIXME Consolidate list handling
	private fun RaptorGraphOutputScope.serializeAliasValue(value: Any, serialize: RaptorGraphOutputScope.(value: Any) -> Any?, typeRef: GTypeRef): Any? =
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
