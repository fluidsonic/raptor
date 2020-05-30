package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.graphql.ifNull
import io.fluidsonic.stdlib.*
import org.slf4j.*
import kotlin.reflect.*


internal class GraphSystem(
	val fieldDefinitionExtensionKey: FieldDefinitionExtensionKey,
	val schema: GSchema,
	val typeDefinitionExtensionKey: TypeDefinitionExtensionKey,
	val valueTypeExtensionKey: ValueTypeExtensionKey
) {

	fun createNodeInputCoercion(): GNodeInputCoercion<RaptorGraphContext> =
		NodeInputCoercion()


	fun createFieldResolver(): GFieldResolver<RaptorGraphContext, Any> =
		FieldResolver()


	// FIXME use decorator for default instead of re-implementing default behavior
	private inner class FieldResolver : GFieldResolver<RaptorGraphContext, Any> {

		override suspend fun resolveField(parent: Any, context: GFieldResolverContext<RaptorGraphContext>): Any? {
			try {
				val definition = context.fieldDefinition[fieldDefinitionExtensionKey]
					?: error("Cannot resolve non-existent field '${context.fieldDefinition.name}' of GraphQL type '${context.parentTypeDefinition.name}'.")

				val alias = context.fieldDefinition[typeDefinitionExtensionKey]
					.ifNull { error("Cannot resolve type of field '${context.fieldDefinition.name}' in GraphQL type '${context.parentTypeDefinition.name}'.") }
					.let { it as? GraphAliasDefinition<Any, Any> }
					?.serialize

				val resolve = definition.resolver as (suspend RaptorGraphScope.(Any) -> Any?)?
					?: TODO() // FIXME default resolver

				return GraphInputContext(arguments = context.arguments).use {
					with(context.environment.asScope()) {
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


	private inner class NodeInputCoercion : GNodeInputCoercion.Decorator<RaptorGraphContext> {

		override val decorated = GNodeInputCoercion.default<RaptorGraphContext>()


		override fun coerceValue(
			value: GValue?,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<RaptorGraphContext>
		): Any? {
			val valueType = argument[valueTypeExtensionKey]
			val expectsMaybe = valueType?.classifier == Maybe::class && typeRef == argument.type // FIXME also support for variables

			if (expectsMaybe && value == null)
				return Maybe.nothing

			var coercedValue = super.coerceValue(value, typeRef, parentType, field, argument, context)
			if (coercedValue != null && typeRef is GNamedTypeRef) {
				@Suppress("UNCHECKED_CAST")
				val aliasDefinition = argument[typeDefinitionExtensionKey]
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
					as? GraphAliasDefinition<Any, Any>

				if (aliasDefinition != null)
					coercedValue = aliasDefinition.parse(context.environment, coercedValue)
			}

			if (expectsMaybe)
				coercedValue = Maybe.of(coercedValue)

			return coercedValue
		}
	}


	class FieldDefinitionExtensionKey : GNode.ExtensionKey<GraphObjectDefinition.Field<*, *>>
	class TypeDefinitionExtensionKey : GNode.ExtensionKey<GraphTypeDefinition<*>>
	class ValueTypeExtensionKey : GNode.ExtensionKey<KType>
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
