package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import org.slf4j.*


internal class GraphSystem(
	val fieldDefinitionExtensionKey: FieldDefinitionExtensionKey,
	val schema: GSchema,
	val typeDefinitionExtensionKey: TypeDefinitionExtensionKey
) {

	fun createNodeInputCoercion(scope: RaptorGraphScope): GNodeInputCoercion<RaptorGraphScope> =
		NodeInputCoercion(scope = scope)


	fun createFieldResolver(scope: RaptorGraphScope): GFieldResolver<RaptorGraphScope, Any> =
		FieldResolver(scope = scope)


	// FIXME use decorator for default instead of re-implementing default behavior
	private inner class FieldResolver(
		private val scope: RaptorGraphScope
	) : GFieldResolver<Any, Any> {

		override suspend fun resolveField(parent: Any, context: GFieldResolverContext<Any>): Any? {
			try {
				val definition = context.fieldDefinition[fieldDefinitionExtensionKey]
					?: error("Cannot resolve non-existent field '${context.fieldDefinition.name}' of GraphQL type '${context.parentTypeDefinition.name}'.")

				val alias = context.fieldDefinition[typeDefinitionExtensionKey]
					.ifNull { error("Cannot resolve type of field '${context.fieldDefinition.name}' in GraphQL type '${context.parentTypeDefinition.name}'.") }
					.let { it as? GraphAliasDefinition<Any, Any> }
					?.serialize

				val resolve = definition.resolve as (suspend RaptorGraphScope.(Any) -> Any?)?
					?: TODO() // FIXME default resolver

				return GraphInputContext(arguments = context.arguments).use {
					with(scope) {
						resolve(parent)?.let { resolvedValue ->
							when {
								alias !== null -> serializeAlias(value = resolvedValue, serialize = alias, typeRef = context.fieldDefinition.type)
								else -> resolvedValue
							}
						}
					}
				}
			}
			catch (e: GError) {
				throw e
			}
			catch (e: Throwable) {
				throw resolveFieldError(e)
			}
		}


		private fun resolveFieldError(throwable: Throwable): GError {
			val failure = throwable as? ServerFailure ?: ServerFailure.internal(throwable)
			LoggerFactory.getLogger(GraphSystem::class.java).error("Endpoint failure", failure) // FIXME

			return GError(
				message = failure.developerMessage,
				cause = failure
			)
		}
	}


	private inner class NodeInputCoercion(
		private val scope: RaptorGraphScope
	) : GNodeInputCoercion.Decorator<RaptorGraphScope> {

		override val decorated = GNodeInputCoercion.default<RaptorGraphScope>()


		override fun coerceValue(
			value: GValue?,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<RaptorGraphScope>
		): Any? {
			val coercedValue = super.coerceValue(value, typeRef, parentType, field, argument, context)
				?: return null

			if (typeRef !is GNamedTypeRef)
				return coercedValue

			val typeDefinition = argument[typeDefinitionExtensionKey]
				.ifNull {
					return decorated.coerceValue(
						value = value,
						typeRef = typeRef,
						parentType = parentType,
						field = field,
						argument = argument,
						context = context
					)
				}
				.let { it as? GraphAliasDefinition<Any, Any> }
				?: return coercedValue

			return typeDefinition.parse(scope, coercedValue)
		}
	}


	class FieldDefinitionExtensionKey : GNode.ExtensionKey<GraphObjectDefinition.Field<*, *>>
	class TypeDefinitionExtensionKey : GNode.ExtensionKey<GraphTypeDefinition<*>>
}


internal fun GraphSystem(definitions: Collection<RaptorGraphDefinition>) =
	GraphSystemBuilder()
		.apply {
			for (definition in definitions)
				add(definition)
		}
		.build()


// FIXME Consolidate list handling
private tailrec fun RaptorGraphScope.serializeAlias(value: Any, serialize: RaptorGraphScope.(value: Any) -> Any?, typeRef: GTypeRef): Any? =
	when (typeRef) {
		is GListTypeRef -> when (value) {
			is Iterable<*> -> value.map { element ->
				element?.let { serializeAlias(value = element, serialize = serialize, typeRef = typeRef.elementType) }
			}
			else -> value
		}
		is GNamedTypeRef -> serialize(value)
		is GNonNullTypeRef -> serializeAlias(value = value, serialize = serialize, typeRef = typeRef.nullableRef)
	}
