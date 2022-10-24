package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*


internal object VariableInputCoercer : GVariableInputCoercer<Any?> {

	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any?): Any? {
		return next()

		// TODO why is this needed here?
//		var coercedValue = super.coerceValue(value, typeRef, variable, context)
//		if (coercedValue != null && typeRef is GNamedTypeRef) {
//			val typeDefinition = typeDefinitionsByGraphName[typeRef.underlyingName] // TODO lists and non-null
//			// TODO refactor
//			coercedValue = when (typeDefinition) {
//				null -> coercedValue
//				is GraphEnumDefinition -> typeDefinition.values.filterIsInstance<Enum<*>>().first { it.name == value }
//				is GraphInputObjectDefinition -> GraphInputContext(
//					arguments = value as Map<String, Any?>,
//					definitions = (context.schema.resolveType(typeRef.name) as GInputObjectType).argumentDefinitions,
//					environment = context.environment,
//					system = this@GraphSystem
//				).useBlocking {
//					with(typeDefinition) {
//						with(context.environment as RaptorGraphScope) { factory() }
//					}
//				}
//				is GraphInterfaceDefinition -> error("Interface type '$typeRef' cannot appear in an input position.")
//				is GraphObjectDefinition -> error("Object type '$typeRef' cannot appear in an input position.")
//				is GraphScalarDefinition -> typeDefinition.parseJson(context.environment, coercedValue)
//			}
//		}
//
//		return coercedValue
	}
}
