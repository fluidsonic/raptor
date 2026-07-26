package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


internal object FieldResolver : GFieldResolver<Any> {

	override suspend fun GFieldResolverContext.resolveField(parent: Any): Any? {
		val context = checkNotNull(execution.raptorContext)
		val field = checkNotNull(fieldDefinition.raptorField) as GraphField.Resolvable
		val resolve = checkNotNull(field.resolve)
		val argumentResolver = checkNotNull(field.argumentResolver)

		// Unlike coercion, a field resolver may use the transaction — and thus dependency injection.
		val resolverScope = object : RaptorGraphResolverScope, RaptorTransactionScope by context {}

		val value = argumentResolver.withArguments(
			argumentValues = arguments,
			argumentDefinitions = fieldDefinition.argumentDefinitions,
		) { resolve(resolverScope, parent) }
			?: return null

		val aliasType = fieldDefinition.raptorType as? AliasGraphType
			?: return value

		return GraphOutputScope.serializeAliasValue(value, serialize = aliasType.convertAliasToReferenced, typeRef = fieldDefinition.type)
	}


	// TODO Consolidate list handling
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
